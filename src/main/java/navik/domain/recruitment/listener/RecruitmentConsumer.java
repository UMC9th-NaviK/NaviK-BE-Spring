package navik.domain.recruitment.listener;

import static navik.domain.recruitment.scheduler.RecruitmentPendingScheduler.*;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Iterator;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.service.recruitment.RecruitmentCommandService;

@Slf4j
@Component
@RequiredArgsConstructor

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
	 * TODO: 멱등성 보장 로직 + 죽은 메시지를 저장하여 나중에 관리 창에서 확인할 수 있도록 로직 추가
	 */
	@Override
	@Transactional
	public void onMessage(ObjectRecord<String, String> message) {
		log.info("[RecruitmentConsumer] 메시지를 수신하였습니다.");
		String receivedStreamKey = message.getStream();
		String recordId = message.getId().getValue();

		try {
			log.info("[RecruitmentConsumer] 채용 공고 적재 시도");
			if (StringUtils.isNotEmpty(receivedStreamKey) && StringUtils.isNotEmpty(recordId)) {
				RecruitmentRequestDTO.Recruitment recruitmentDTO = objectMapper.readValue(
					message.getValue(),
					RecruitmentRequestDTO.Recruitment.class
				);
				saveRecruitment(recruitmentDTO);
				redisTemplate.opsForStream().acknowledge(receivedStreamKey, consumerGroupName, recordId);  // ACK
				log.info("[RecruitmentConsumer] 채용 공고 적재 완료하였습니다.");
			} else {
				log.error("[RecruitmentConsumer] streamKey 또는 recordId가 비어있습니다.");
			}
		} catch (Exception e) {
			// 처리 오류 발생 시 자체 ErrorCount++
			redisTemplate.opsForValue().increment(RETRY_COUNT_KEY + recordId);
			log.error("[RecruitmentConsumer] 채용 공고 처리 중 오류 발생 ErrorCount++ : {}", e.getMessage(), e);
		}
	}

	/**
	 * 채용 공고 DTO를 받아 저장합니다.
	 */
	public void saveRecruitment(RecruitmentRequestDTO.Recruitment recruitmentDTO) {
		recruitmentCommandService.saveRecruitment(recruitmentDTO);
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
}
