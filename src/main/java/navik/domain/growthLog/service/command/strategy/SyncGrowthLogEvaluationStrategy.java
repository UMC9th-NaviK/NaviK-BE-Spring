package navik.domain.growthLog.service.command.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.internal.Evaluated;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "sync", matchIfMissing = true)
public class SyncGrowthLogEvaluationStrategy implements GrowthLogEvaluationStrategy {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogEvaluationCoreService core;
	private final GrowthLogPersistenceService growthLogPersistenceService;
	private final RetryRateLimiter retryRateLimiter;

	@Override
	public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {

		String inputContent = safe(req.content());

		try {
			var context = core.buildContext(userId, inputContent);
			Evaluated evaluated = core.evaluate(userId, context);

			Long id = growthLogPersistenceService.saveUserInputLog(
				userId,
				evaluated.normalized(),
				evaluated.totalDelta()
			);

			return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.COMPLETED);

		} catch (Exception e) {
			Long id = growthLogPersistenceService.saveFailedUserInputLog(userId, inputContent);
			return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.FAILED);
		}
	}

	@Override
	public GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId) {

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		if (growthLog.getType() != GrowthType.USER_INPUT) {
			throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_TYPE);
		}

		String key = "growthLogRetry:" + userId + ":" + growthLogId;
		if (!retryRateLimiter.tryAcquire(key, 3)) {
			throw new GeneralException(GrowthLogErrorCode.GROWTH_LOG_RETRY_LIMIT_EXCEEDED);
		}

		// 동시 재시도 차단: FAILED -> PENDING 원자적 전환
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.FAILED,
			GrowthLogStatus.PENDING
		);

		if (acquired == 0) {
			throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		try {
			var context = core.buildContext(userId, safe(growthLog.getContent()));
			Evaluated evaluated = core.evaluate(userId, context);

			growthLogPersistenceService.updateGrowthLogAfterRetry(
				userId,
				growthLogId,
				evaluated.normalized(),
				evaluated.totalDelta()
			);

			return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.COMPLETED);

		} catch (Exception e) {
			// 실패 시 상태를 FAILED로 복구
			growthLogRepository.updateStatusIfMatch(
				userId,
				growthLogId,
				GrowthLogStatus.PENDING,
				GrowthLogStatus.FAILED
			);
			return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.FAILED);
		}
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}
}
