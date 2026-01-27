package navik.domain.growthLog.service.command;

import java.util.UUID;

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
import navik.domain.growthLog.message.GrowthLogEvaluationMessage;
import navik.domain.growthLog.message.GrowthLogEvaluationPublisher;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "async")
public class AsyncGrowthLogEvaluationStrategy implements GrowthLogEvaluationStrategy {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogPersistenceService growthLogPersistenceService;
	private final GrowthLogEvaluationPublisher publisher;
	private final RetryRateLimiter retryRateLimiter;

	@Override
	public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {

		String inputContent = safe(req.content());

		// 1) PENDING 저장
		Long id = growthLogPersistenceService.savePendingUserInputLog(userId, inputContent);

		// 2) enqueue
		publisher.publish(new GrowthLogEvaluationMessage(userId, id, UUID.randomUUID().toString()));

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

		// FAILED -> PENDING 원자 전환 (이미 너가 잘 구현한 패턴)
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.FAILED,
			GrowthLogStatus.PENDING
		);

		if (acquired == 0) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		// enqueue
		publisher.publish(new GrowthLogEvaluationMessage(userId, growthLogId, UUID.randomUUID().toString()));

		return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.PENDING);
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}
}
