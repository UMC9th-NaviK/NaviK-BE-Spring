package navik.domain.growthLog.service.command.strategy;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.exception.code.GrowthLogRedisErrorCode;
import navik.domain.growthLog.message.GrowthLogEvaluationMessage;
import navik.domain.growthLog.message.GrowthLogEvaluationPublisher;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "async")
public class AsyncGrowthLogEvaluationStrategy implements GrowthLogEvaluationStrategy {

	private static final Logger log = LoggerFactory.getLogger(AsyncGrowthLogEvaluationStrategy.class);

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogPersistenceService growthLogPersistenceService;
	private final GrowthLogEvaluationPublisher publisher;
	private final RetryRateLimiter retryRateLimiter;

	@Override
	public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {

		String inputContent = safe(req.content());

		// 1) PENDING 저장
		Long id = growthLogPersistenceService.savePendingUserInputLog(userId, inputContent);

		// 2) enqueue (실패 시 FAILED 보상 + 에러코드 통일)
		publishWithCompensation(
			userId,
			id,
			GrowthLogStatus.PENDING,
			GrowthLogStatus.FAILED
		);

		// 3) 즉시 응답
		return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.PENDING);
	}

	@Override
	public GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId) {

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		if (growthLog.getType() != GrowthType.USER_INPUT) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_TYPE);
		}

		String key = "growthLogRetry:" + userId + ":" + growthLogId;
		if (!retryRateLimiter.tryAcquire(key, 3)) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_RETRY_LIMIT_EXCEEDED);
		}

		// FAILED -> PENDING 원자 전환
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.FAILED,
			GrowthLogStatus.PENDING
		);

		if (acquired == 0) {
			// 이미 PENDING/COMPLETED 등으로 바뀌었거나, 동시성으로 선점됨
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		// enqueue (실패 시 FAILED 보상 + 에러코드 통일)
		publishWithCompensation(
			userId,
			growthLogId,
			GrowthLogStatus.PENDING,
			GrowthLogStatus.FAILED
		);

		return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.PENDING);
	}

	/**
	 * Redis Stream publish 시도.
	 * 실패하면 상태를 (from -> to)로 보상 업데이트하고, API 레벨은 통일된 ErrorCode로 던진다.
	 */
	private void publishWithCompensation(
		Long userId,
		Long growthLogId,
		GrowthLogStatus from,
		GrowthLogStatus to
	) {
		String traceId = UUID.randomUUID().toString();
		String processingToken = UUID.randomUUID().toString();

		try {
			growthLogRepository.overwriteProcessingToken(userId, growthLogId, processingToken);

			publisher.publish(new GrowthLogEvaluationMessage(userId, growthLogId, traceId, processingToken));

		} catch (Exception e) {

			int rolledBack = growthLogRepository.updateStatusIfMatch(userId, growthLogId, from, to);

			if (rolledBack == 0) {
				log.warn(
					"Redis Stream 발행 실패: 성장 로그 상태 보상 실패. " +
						"이미 상태가 변경되었거나 DB 이슈 가능. " +
						"userId={}, growthLogId={}, from={}, to={}, traceId={}, token={}",
					userId, growthLogId, from, to, traceId, processingToken, e
				);

			} else {
				log.error(
					"Redis Stream 발행 실패: 성장 로그 평가 메시지 enqueue 중 오류 발생 (상태 보상 완료). " +
						"userId={}, growthLogId={}, from={}, to={}, traceId={}, token={}",
					userId, growthLogId, from, to, traceId, processingToken, e
				);
			}

			throw new GeneralExceptionHandler(GrowthLogRedisErrorCode.STREAM_PUBLISH_FAILED);
		}
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}
}
