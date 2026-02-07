package navik.domain.portfolio.message;

import java.util.Map;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.exception.code.PortfolioRedisErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPortfolioAnalysisPublisher implements PortfolioAnalysisPublisher {

	private static final String STREAM_KEY = "portfolio:analyze";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void publish(PortfolioAnalysisMessage message) {

		// 메세지 구성 (최소 식별자 사용)
		Map<String, String> fields = Map.of("userId", String.valueOf(message.userId()), "portfolioId",
			String.valueOf(message.portfolioId()), "traceId", message.traceId());

		// 레디스 스트림 메시지 발행
		RecordId recordId = redisTemplate.opsForStream().add(MapRecord.create(STREAM_KEY, fields));

		// 발행 실패시
		if (recordId == null) {
			throw new GeneralException(PortfolioRedisErrorCode.STREAM_PUBLISH_FAILED);
		}

		log.info("[PortfolioAnalysis] published. traceId={}, userId={}, portfolioId={}", message.traceId(),
			message.userId(), message.portfolioId());
	}
}
