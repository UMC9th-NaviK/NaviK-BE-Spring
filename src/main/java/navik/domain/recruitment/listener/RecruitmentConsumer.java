package navik.domain.recruitment.listener;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.service.recruitment.RecruitmentCommandService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitmentConsumer implements StreamListener<String, ObjectRecord<String, String>>, InitializingBean,
	DisposableBean {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RecruitmentCommandService recruitmentCommandService;
	private final ObjectMapper objectMapper;

	@Value("${spring.data.redis.stream.recruitment.key}")
	private String streamKey;
	@Value("${spring.data.redis.stream.recruitment.group-name}")
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

		// Consumer Name 설정
		this.consumerName =
			InetAddress.getLocalHost().getHostName() + ":" + UUID.randomUUID().toString().substring(0, 8);

		// StreamMessageListenerContainer 설정
		this.listenerContainer = StreamMessageListenerContainer.create(
			redisTemplate.getConnectionFactory(),
			StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
				.targetType(String.class)
				.build()
		);

		// Subscription 설정
		this.subscription = this.listenerContainer.receive(
			Consumer.from(this.consumerGroupName, consumerName),
			StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
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
		String receivedStreamKey = message.getStream();
		String recordId = message.getId().getValue();

		try {
			if (StringUtils.isNotEmpty(receivedStreamKey) && StringUtils.isNotEmpty(recordId)) {
				RecruitmentRequestDTO.Recruitment recruitmentDTO = objectMapper.readValue(message.getValue(),
					RecruitmentRequestDTO.Recruitment.class);
				recruitmentCommandService.saveRecruitment(recruitmentDTO);
				redisTemplate.opsForStream().acknowledge(receivedStreamKey, consumerGroupName, recordId);
			} else {
				log.error("[RecruitmentConsumer] streamKey 또는 recordId가 비어있습니다.");
			}
		} catch (Exception e) {
			log.error("[RecruitmentConsumer] 채용 공고 처리 중 오류 발생", e);
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
	 * Consume을 위해 Stream과 ConsumerGroup 존재 여부를 확인하고, 생성합니다.
	 */
	private void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
		if (!redisTemplate.hasKey(streamKey)) {
			RedisClusterAsyncCommands commands = (RedisClusterAsyncCommands)this.redisTemplate
				.getConnectionFactory()
				.getClusterConnection()
				.getNativeConnection();

			CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
				.add(CommandKeyword.CREATE)
				.add(streamKey)
				.add(consumerGroupName)
				.add("0")
				.add("MKSTREAM");

			commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8), args);
		} else {
			if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
				this.redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
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
