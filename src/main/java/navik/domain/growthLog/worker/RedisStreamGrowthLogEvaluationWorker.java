package navik.domain.growthLog.worker;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.worker.enabled", havingValue = "true")
public class RedisStreamGrowthLogEvaluationWorker {

	private static final String STREAM_KEY = "growthlog:evaluate";
	private static final String GROUP = "growthlog-eval";

	private final StringRedisTemplate redisTemplate;
	private final GrowthLogEvaluationWorkerProcessor processor;

	private final String consumerName = "worker-" + UUID.randomUUID();

	@Value("${navik.growth-log.worker.batch-size:10}")
	private int batchSize;

	@Value("${navik.growth-log.worker.block-ms:2000}")
	private long blockMs;

	@Value("${navik.growth-log.worker.claim.min-idle-ms:60000}")
	private long claimMinIdleMs;

	@Value("${navik.growth-log.worker.claim.batch-size:10}")
	private int claimBatchSize;

	@PostConstruct
	public void initGroupIfNeeded() {
		try {
			redisTemplate.opsForStream().add(MapRecord.create(STREAM_KEY, Map.of("init", "true")));
			redisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
			log.info("[GrowthLogWorker] created group. stream={}, group={}", STREAM_KEY, GROUP);
		} catch (Exception e) {
			log.info("[GrowthLogWorker] group init skipped. stream={}, group={}, reason={}",
				STREAM_KEY, GROUP, e.getMessage());
		}
	}

	@Scheduled(fixedDelayString = "${navik.growth-log.worker.poll-ms:1000}")
	public void poll() {
		Consumer consumer = Consumer.from(GROUP, consumerName);

		// 1) PEL(미처리) 우선 회수해서 처리
		int recovered = recoverPending(consumer);
		if (recovered > 0) {
			// PEL을 처리했으면 바로 다음 스케줄에서 새 메시지 처리로 넘어가도 됨
			// (원하면 여기서 바로 새 메시지도 이어서 처리 가능)
			return;
		}

		// 2) 새 메시지 처리
		List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
			consumer,
			StreamReadOptions.empty().count(batchSize).block(Duration.ofMillis(blockMs)),
			StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
		);

		handle(messages);
	}

	/**
	 * 미처리 메시지(PEL) 복구:
	 * - XPENDING으로 일부 조회
	 * - min-idle 이상인 것만 XCLAIM으로 우리 consumer로 가져옴
	 * - 가져온 레코드 처리 + ACK
	 */
	private int recoverPending(Consumer consumer) {
		try {
			PendingMessages pending = redisTemplate.opsForStream().pending(
				STREAM_KEY,
				GROUP,
				Range.unbounded(),
				claimBatchSize
			);

			if (pending == null || pending.isEmpty())
				return 0;

			// min-idle 이상인 레코드만 claim 대상
			List<RecordId> claimIds = pending.stream()
				.filter(pm -> pm.getElapsedTimeSinceLastDelivery().toMillis() >= claimMinIdleMs)
				.map(PendingMessage::getId)
				.collect(Collectors.toList());

			if (claimIds.isEmpty())
				return 0;

			List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream().claim(
				STREAM_KEY,
				GROUP,
				consumer.getName(),
				Duration.ofMillis(claimMinIdleMs),
				claimIds.toArray(new RecordId[0])
			);

			if (claimed == null || claimed.isEmpty())
				return 0;

			log.info("[GrowthLogWorker] recovered pending messages. count={}, consumer={}",
				claimed.size(), consumer.getName());

			handle(claimed);
			return claimed.size();

		} catch (Exception e) {
			log.warn("[GrowthLogWorker] recoverPending failed. err={}", e.toString());
			return 0;
		}
	}

	private void handle(List<MapRecord<String, Object, Object>> messages) {
		if (messages == null || messages.isEmpty())
			return;

		for (MapRecord<String, Object, Object> msg : messages) {
			String recordId = msg.getId().getValue();
			Map<Object, Object> body = msg.getValue();

			Long userId = parseLong(body.get("userId"));
			Long growthLogId = parseLong(body.get("growthLogId"));
			String traceId = String.valueOf(body.getOrDefault("traceId", ""));
			String processingToken = String.valueOf(body.getOrDefault("processingToken", ""));

			if (userId == null || growthLogId == null || processingToken.isBlank()) {
				log.warn("[GrowthLogWorker] invalid message. recordId={}, body={}", recordId, body);
				ack(recordId);
				continue;
			}

			try {
				var result = processor.process(userId, growthLogId, traceId, processingToken);

				switch (result) {
					case COMPLETED -> log.info(
						"[GrowthLogWorker] completed. traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
					case SKIP_ALREADY_COMPLETED -> log.debug(
						"[GrowthLogWorker] skip (already completed). traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
					case SKIP_NOT_PROCESSING -> log.warn(
						"[GrowthLogWorker] skip (not processing status). traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
					case SKIP_TOKEN_MISMATCH -> log.warn(
						"[GrowthLogWorker] skip (token mismatch). traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
					case SKIP_ALREADY_APPLYING -> log.debug(
						"[GrowthLogWorker] skip (already applying by another worker). traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
					case SKIP_NOT_FOUND -> log.warn(
						"[GrowthLogWorker] skip (not found). traceId={}, userId={}, growthLogId={}",
						traceId, userId, growthLogId);
				}

				ack(recordId);

			} catch (Exception e) {
				processor.markFailedIfProcessing(userId, growthLogId, processingToken);

				log.error("[GrowthLogWorker] failed. traceId={}, userId={}, growthLogId={}, recordId={}",
					traceId, userId, growthLogId, recordId, e);

				ack(recordId);
			}
		}
	}

	private void ack(String recordId) {
		try {
			redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP, recordId);
		} catch (Exception e) {
			log.warn("[GrowthLogWorker] ack failed. recordId={}, err={}", recordId, e.toString());
		}
	}

	private Long parseLong(Object v) {
		if (v == null)
			return null;
		try {
			return Long.parseLong(String.valueOf(v));
		} catch (Exception e) {
			return null;
		}
	}
}
