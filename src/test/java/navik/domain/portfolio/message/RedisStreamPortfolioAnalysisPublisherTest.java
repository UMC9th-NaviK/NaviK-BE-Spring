package navik.domain.portfolio.message;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import navik.domain.portfolio.exception.code.PortfolioRedisErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@ExtendWith(MockitoExtension.class)
class RedisStreamPortfolioAnalysisPublisherTest {

	@Mock
	StringRedisTemplate redisTemplate;

	@Mock
	StreamOperations<String, Object, Object> streamOperations;

	@InjectMocks
	RedisStreamPortfolioAnalysisPublisher publisher;

	@Nested
	@DisplayName("publish()")
	class Publish {

		@Test
		@DisplayName("메시지 발행 성공")
		void success() {
			// given
			var message = new PortfolioAnalysisMessage(1L, 10L, "trace-id");

			given(redisTemplate.opsForStream()).willReturn(streamOperations);
			given(streamOperations.add(any(MapRecord.class))).willReturn(RecordId.of("1234-0"));

			// when & then
			assertThatCode(() -> publisher.publish(message)).doesNotThrowAnyException();

			verify(streamOperations).add(any(MapRecord.class));
		}

		@Test
		@DisplayName("RecordId가 null이면 STREAM_PUBLISH_FAILED 예외 발생")
		void recordIdNull_throws() {
			// given
			var message = new PortfolioAnalysisMessage(1L, 10L, "trace-id");

			given(redisTemplate.opsForStream()).willReturn(streamOperations);
			given(streamOperations.add(any(MapRecord.class))).willReturn(null);

			// when & then
			assertThatThrownBy(() -> publisher.publish(message))
				.isInstanceOf(GeneralExceptionHandler.class)
				.satisfies(ex -> {
					GeneralExceptionHandler handler = (GeneralExceptionHandler) ex;
					assertThat(handler.getCode()).isEqualTo(PortfolioRedisErrorCode.STREAM_PUBLISH_FAILED);
				});
		}
	}
}
