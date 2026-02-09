package navik.domain.growthLog.message;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.exception.code.GrowthLogRedisErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "async")
public class RedisStreamGrowthLogEvaluationPublisher implements GrowthLogEvaluationPublisher {

	private static final String STREAM_KEY = "growthlog:evaluate";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void publish(GrowthLogEvaluationMessage message) {
		Map<String, String> fields = Map.of(
			"userId", String.valueOf(message.userId()),
			"growthLogId", String.valueOf(message.growthLogId()),
			"traceId", message.traceId(),
			"processingToken", message.processingToken()
		);

		RecordId recordId = redisTemplate.opsForStream()
			.add(MapRecord.create(STREAM_KEY, fields));

		if (recordId == null) {
			throw new GeneralException(GrowthLogRedisErrorCode.STREAM_PUBLISH_FAILED);
		}
	}
}
