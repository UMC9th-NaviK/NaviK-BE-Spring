package navik.domain.growthLog.service.command.strategy;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "async")
@Transactional
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
		publishWithCompensation(userId, id);

		// 3) 즉시 응답
		return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.PENDING);
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

		// FAILED -> PENDING 원자 전환
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId, growthLogId,
			GrowthLogStatus.FAILED,
			GrowthLogStatus.PENDING
		);

		if (acquired == 0) {
			// 이미 PENDING/COMPLETED 등으로 바뀌었거나, 동시성으로 선점됨
			throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		// enqueue (실패 시 FAILED 보상 + 에러코드 통일)
		publishWithCompensation(userId, growthLogId);

		return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.PENDING);
	}

	/**
	 * Redis Stream publish 시도.
	 * 실패하면 상태를 (from -> to)로 보상 업데이트하고, API 레벨은 통일된 ErrorCode로 던진다.
	 */
	private void publishWithCompensation(Long userId, Long growthLogId) {
		String traceId = UUID.randomUUID().toString();
		String processingToken = UUID.randomUUID().toString();
		GrowthLogStatus currentStatus = GrowthLogStatus.PENDING;

		try {
			// 1) processingToken 저장
			int tokenSet = growthLogRepository.overwriteProcessingToken(userId, growthLogId, processingToken);
			if (tokenSet == 0) {
				throw new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND);
			}

			// 2) PENDING -> PROCESSING 전환 (token 조건 포함)
			int moved = growthLogRepository.updateStatusIfMatchAndToken(
				userId,
				growthLogId,
				GrowthLogStatus.PENDING,
				GrowthLogStatus.PROCESSING,
				processingToken
			);

			if (moved == 0) {
				throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
			}

			currentStatus = GrowthLogStatus.PROCESSING;

			// 3) Redis Stream publish (token 포함 메시지)
			publisher.publish(new GrowthLogEvaluationMessage(userId, growthLogId, traceId, processingToken));

		} catch (Exception e) {

			int rolledBack = growthLogRepository.updateStatusIfMatch(
				userId, growthLogId, currentStatus, GrowthLogStatus.FAILED
			);

			if (rolledBack == 0) {
				log.warn(
					"Redis Stream 발행 실패: 상태 보상 실패. userId={}, growthLogId={}, currentStatus={}, traceId={}, token={}",
					userId, growthLogId, currentStatus, traceId, processingToken, e
				);

			} else {
				log.error(
					"Redis Stream 발행 실패 (상태 보상 완료). userId={}, growthLogId={}, currentStatus={}, traceId={}, token={}",
					userId, growthLogId, currentStatus, traceId, processingToken, e
				);
			}

			throw new GeneralException(GrowthLogRedisErrorCode.STREAM_PUBLISH_FAILED);
		}
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}
}
