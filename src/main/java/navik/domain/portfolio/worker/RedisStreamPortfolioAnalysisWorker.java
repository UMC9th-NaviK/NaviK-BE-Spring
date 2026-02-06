package navik.domain.portfolio.worker;

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
@ConditionalOnProperty(name = "navik.portfolio.analysis.worker.enabled", havingValue = "true")
public class RedisStreamPortfolioAnalysisWorker {

	private static final String STREAM_KEY = "portfolio:analyze";
	private static final String GROUP = "portfolio-analysis";

	private final StringRedisTemplate redisTemplate;
	private final PortfolioAnalysisWorkerProcessor processor;

	private final String consumerName = "worker-" + UUID.randomUUID();

	@Value("${navik.portfolio.analysis.worker.batch-size:10}")
	private int batchSize;

	@Value("${navik.portfolio.analysis.worker.block-ms:2000}")
	private long blockMs;

	@Value("${navik.portfolio.analysis.worker.claim.min-idle-ms:60000}")
	private long claimMinIdleMs;

	@Value("${navik.portfolio.analysis.worker.claim.batch-size:10}")
	private int claimBatchSize;

	@PostConstruct
	public void initGroupIfNeeded() {
		try {
			redisTemplate.opsForStream().add(MapRecord.create(STREAM_KEY, Map.of("init", "true")));
			redisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
			log.info("[PortfolioAnalysisWorker] created group. stream={}, group={}", STREAM_KEY, GROUP);
		} catch (Exception e) {
			log.info("[PortfolioAnalysisWorker] group init skipped. stream={}, group={}, reason={}", STREAM_KEY, GROUP,
				e.getMessage());
		}
	}

	@Scheduled(fixedDelayString = "${navik.portfolio.analysis.worker.poll-ms:1000}")
	public void poll() {
		Consumer consumer = Consumer.from(GROUP, consumerName);

		// 1) PEL 우선 회수
		int recovered = recoverPending(consumer);
		if (recovered > 0) {
			return;
		}

		// 2) 새 메시지 처리
		List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
			.read(consumer, StreamReadOptions.empty().count(batchSize).block(Duration.ofMillis(blockMs)),
				StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));

		handle(messages);
	}

	private int recoverPending(Consumer consumer) {
		try {
			PendingMessages pending = redisTemplate.opsForStream()
				.pending(STREAM_KEY, GROUP, Range.unbounded(), claimBatchSize);

			if (pending == null || pending.isEmpty())
				return 0;

			List<RecordId> claimIds = pending.stream()
				.filter(pm -> pm.getElapsedTimeSinceLastDelivery().toMillis() >= claimMinIdleMs)
				.map(PendingMessage::getId)
				.collect(Collectors.toList());

			if (claimIds.isEmpty())
				return 0;

			List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream()
				.claim(STREAM_KEY, GROUP, consumer.getName(), Duration.ofMillis(claimMinIdleMs),
					claimIds.toArray(new RecordId[0]));

			if (claimed == null || claimed.isEmpty())
				return 0;

			log.info("[PortfolioAnalysisWorker] recovered pending messages. count={}, consumer={}", claimed.size(),
				consumer.getName());

			handle(claimed);
			return claimed.size();

		} catch (Exception e) {
			log.warn("[PortfolioAnalysisWorker] recoverPending failed. err={}", e.toString());
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
			Long portfolioId = parseLong(body.get("portfolioId"));
			String traceId = String.valueOf(body.getOrDefault("traceId", ""));

			if (userId == null || portfolioId == null) {
				log.warn("[PortfolioAnalysisWorker] invalid message. recordId={}, body={}", recordId, body);
				ack(recordId);
				continue;
			}

			try {
				processor.process(userId, portfolioId, traceId);
				ack(recordId);
			} catch (Exception e) {
				log.error("[PortfolioAnalysisWorker] failed. traceId={}, userId={}, portfolioId={}, recordId={}",
					traceId, userId, portfolioId, recordId, e);
				ack(recordId);
			}
		}
	}

	private void ack(String recordId) {
		try {
			redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP, recordId);
		} catch (Exception e) {
			log.warn("[PortfolioAnalysisWorker] ack failed. recordId={}, err={}", recordId, e.toString());
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
