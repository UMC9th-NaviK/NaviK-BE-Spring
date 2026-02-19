package navik.domain.recruitment.listener;

import static navik.domain.recruitment.scheduler.RecruitmentPendingScheduler.*;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Iterator;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.service.recruitment.RecruitmentCommandService;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
public class RecruitmentConsumer
	implements StreamListener<String, ObjectRecord<String, String>>, InitializingBean,
	DisposableBean {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RecruitmentCommandService recruitmentCommandService;
	private final ObjectMapper objectMapper;

	@Value("${spring.data.redis.stream.recruitment.key}")
	private String streamKey;
	@Value("${spring.data.redis.stream.recruitment.consumer-group-name}")
	private String consumerGroupName;
	private String consumerName;
	private Subscription subscription;
	private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

	/**
	 * Bean 생성 시 StreamMessageListenerContainer 및 Subscription 생성
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// Consumer Group 설정
		createStreamConsumerGroup(streamKey, consumerGroupName);

		// Consumer Name 설정 (docker container id)
		this.consumerName = InetAddress.getLocalHost().getHostName();

		// StreamMessageListenerContainer 설정
		this.listenerContainer = StreamMessageListenerContainer.create(
			redisTemplate.getConnectionFactory(),
			StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
				.errorHandler(t -> {
					if (t.getMessage() != null && t.getMessage()
						.contains("UNBLOCKED")) {
						return;
					}
					log.error("Unexpected error in Stream polling", t);
				})
				.pollTimeout(Duration.ofSeconds(10))
				.targetType(String.class)
				.build()
		);

		// Subscription 설정
		this.subscription = this.listenerContainer.receive(
			Consumer.from(this.consumerGroupName, consumerName),
			StreamOffset.create(streamKey, ReadOffset.lastConsumed()), // ">" : 다른 컨슈머가 소비하지 않은 것
			this
		);

		// Redis listen 시작
		this.listenerContainer.start();
	}

	/**
	 * 채용 공고 처리 작업
	 */
	@Override
	public void onMessage(ObjectRecord<String, String> message) {
		log.info("[RecruitmentConsumer] 메시지를 수신하였습니다.");
		String receivedStreamKey = message.getStream();
		String recordId = message.getId().getValue();

		if (StringUtils.isEmpty(receivedStreamKey) || StringUtils.isEmpty(recordId)) {
			log.error("[RecruitmentConsumer] streamKey 또는 recordId가 비어있습니다.");
			return;
		}

		RecruitmentRequestDTO.Recruitment recruitmentDTO = null;

		try {
			recruitmentDTO = objectMapper.readValue(
				message.getValue(),
				RecruitmentRequestDTO.Recruitment.class
			);
		} catch (Exception e) {
			log.error("[RecruitmentConsumer] JSON 파싱 실패: {}", e.getMessage());
			handleError(recordId);
			return;
		}

		try {
			String duplicateCheckKey = "RECRUITMENT:LOCK:" + recruitmentDTO.getPostId();
			Boolean isAcquired = redisTemplate.opsForValue()
				.setIfAbsent(duplicateCheckKey, "PROCESSING", Duration.ofSeconds(60));

			if (Boolean.FALSE.equals(isAcquired)) {
				String currentStatus = (String)redisTemplate.opsForValue().get(duplicateCheckKey);
				if ("DONE".equals(currentStatus)) {
					log.info("[RecruitmentConsumer] 이미 처리 완료된 공고입니다. (ACK 유실 방지)");
					redisTemplate.opsForStream().acknowledge(receivedStreamKey, consumerGroupName, recordId);
				} else {
					log.info("[RecruitmentConsumer] 다른 컨슈머가 현재 처리 중입니다. (Skip)");
				}
				return;
			}

			try {
				log.info("[RecruitmentConsumer] 채용 공고 적재 시작: {}", recruitmentDTO.getPostId());
				recruitmentCommandService.saveRecruitment(recruitmentDTO);
				redisTemplate.opsForValue().set(duplicateCheckKey, "DONE", Duration.ofDays(1));
				redisTemplate.opsForStream().acknowledge(receivedStreamKey, consumerGroupName, recordId);
				redisTemplate.delete(RETRY_COUNT_KEY + recordId);
				log.info("[RecruitmentConsumer] 채용 공고 적재 완료");
			} catch (Exception e) {
				log.warn("[RecruitmentConsumer] 채용 공고 적재 실패로 락 해제");
				redisTemplate.delete(duplicateCheckKey);
				throw e;  // for error count
			}

		} catch (Exception e) {
			log.error("[RecruitmentConsumer] 채용 공고 적재 중 오류 발생 ErrorCount++");
			handleError(recordId);
		}
	}

	/**
	 * 빈 제거 시 작업
	 */
	@Override
	public void destroy() {
		if (this.subscription != null) {
			this.subscription.cancel();
		}
		if (this.listenerContainer != null) {
			this.listenerContainer.stop();
		}
	}

	/**
	 * Consume을 위해 Stream과 ConsumerGroup 존재 여부를 확인하고 생성합니다.
	 */
	private void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
		if (!redisTemplate.hasKey(streamKey)) {
			redisTemplate.opsForStream().createGroup(streamKey, consumerGroupName);    // 비 클러스터
		} else {
			if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
				this.redisTemplate.opsForStream()
					.createGroup(streamKey, ReadOffset.from("0"), consumerGroupName); // 그룹 생성 전 데이터를 포함하는 스트림
			}
		}
	}

	/**
	 * ConsumerGroup 존재 여부 확인
	 */
	private boolean isStreamConsumerGroupExist(String streamKey, String consumerGroupName) {
		Iterator<StreamInfo.XInfoGroup> iterator = this.redisTemplate
			.opsForStream().groups(streamKey).stream().iterator();

		while (iterator.hasNext()) {
			StreamInfo.XInfoGroup xInfoGroup = iterator.next();
			if (xInfoGroup.groupName().equals(consumerGroupName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 에러 발생 시 record retry count 증가
	 */
	private void handleError(String recordId) {
		redisTemplate.opsForValue().increment(RETRY_COUNT_KEY + recordId);
	}
}
