package navik.domain.recruitment.scheduler;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.recruitment.listener.RecruitmentConsumer;

@Slf4j
@Component
@RequiredArgsConstructor

public class RecruitmentPendingScheduler implements InitializingBean {

	public static final int MAX_DELIVERY_COUNT = 3;
	public static final int MAX_RETRY_COUNT = 3;
	public static final String RETRY_COUNT_KEY = "recruitment:try:";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RecruitmentConsumer recruitmentConsumer;

	@Value("${spring.data.redis.stream.recruitment.key}")
	private String streamKey;
	@Value("${spring.data.redis.stream.recruitment.consumer-group-name}")
	private String consumerGroupName;
	private String consumerName;

	@Override
	public void afterPropertiesSet() throws Exception {
		// docker container id
		this.consumerName = InetAddress.getLocalHost().getHostName();
	}

	/**
	 * 지정된 주기로 Pending 메시지를 재처리 시도합니다.
	 */
	@Scheduled(fixedRate = 60000)
	public void retryPendingMessage() {

		// 1. 그룹 내 pending 메시지 요약본 조회
		PendingMessagesSummary pendingSummary = redisTemplate.opsForStream().pending(
			streamKey,
			consumerGroupName
		);

		// 2. 로그 출력
		if (pendingSummary == null || pendingSummary.getTotalPendingMessages() == 0) {
			log.info("[RecruitmentPendingScheduler] 미처리된 채용 공고는 없습니다.");
			return;
		}
		log.info("[RecruitmentPendingScheduler] 미처리된 채용 공고가 총 {}건 존재합니다.", pendingSummary.getTotalPendingMessages());

		// 3. 재시도
		PendingMessages pendingMessages = redisTemplate.opsForStream().pending(
			streamKey,
			consumerGroupName,    // 현재 그룹 내 Pending
			Range.unbounded(),    // - + 전체 구간
			10L                   // 최대 10건
		);

		for (PendingMessage pendingMessage : pendingMessages) {
			String recordId = pendingMessage.getId().getValue();

			// Consumer와 동시 처리 문제 방지
			if (pendingMessage.getElapsedTimeSinceLastDelivery().toMinutes() < 1) {
				continue;
			}

			// 처리 시도
			try {
				// consumer delivery 카운트 및 자체 에러 카운트 검사
				if (!canProcess(pendingMessage, recordId)) {
					redisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
					redisTemplate.delete(RETRY_COUNT_KEY + recordId);
					log.info("[RecruitmentPendingScheduler] 재처리 불가능하여 강제 ACK 처리되었습니다.");
					continue;
				}

				// 현재 컨슈머가 소유 및 처리
				List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().claim(
					streamKey,
					consumerGroupName,
					consumerName,
					Duration.ofMinutes(1),  // 전달된지 1분 이상 지난 메시지 처리 (Consumer와 동시 처리 문제 방지)
					pendingMessage.getId()
				);

				if (records.isEmpty()) {
					continue;
				}

				log.info("[RecruitmentPendingScheduler] 미처리된 채용 공고 적재를 재시도합니다.");

				// onMessage 시도
				recruitmentConsumer.onMessage(convertRecord(records.getFirst()));
				redisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
				log.info("[RecruitmentPendingScheduler] 재시도 처리에 성공하였습니다.");

			} catch (Exception e) {
				log.error("[RecruitmentPendingScheduler] 재시도 처리 중 에러 발생: {}", e.getMessage(), e);
				redisTemplate.opsForValue().increment(RETRY_COUNT_KEY + recordId);
				log.info("[RecruitmentPendingScheduler] 재시도 처리 중 에러 발생 ErrorCount++");
			}
		}
	}

	/**
	 * Pending 메시지의 재처리 가능 여부를 판단하는 메서드입니다.
	 */
	private boolean canProcess(PendingMessage pendingMessage, String recordId) {
		String value = (String)redisTemplate.opsForValue().get(RETRY_COUNT_KEY + recordId);
		int errorCount = value == null ? 0 : Integer.parseInt(value);
		if (errorCount >= MAX_RETRY_COUNT) {
			log.info("[RecruitmentPendingScheduler] 재처리 최대 시도 횟수가 초과되었습니다.");
			return false;
		}
		if (pendingMessage.getTotalDeliveryCount() >= MAX_DELIVERY_COUNT) {
			log.info("[RecruitmentPendingScheduler] 컨슈머 최대 전달 횟수가 초과되었습니다.");
			return false;
		}
		return true;
	}

	private ObjectRecord<String, String> convertRecord(MapRecord<String, Object, Object> record) {
		try {
			String json = objectMapper.writeValueAsString(record.getValue());
			return StreamRecords.newRecord()
				.ofObject(json)
				.withStreamKey(streamKey)
				.withId(record.getId());
		} catch (Exception e) {
			throw new IllegalStateException("Redis Stream record JSON 변환 실패", e);
		}
	}
}
