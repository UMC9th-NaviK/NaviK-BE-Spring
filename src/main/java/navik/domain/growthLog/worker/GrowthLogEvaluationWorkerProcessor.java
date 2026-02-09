package navik.domain.growthLog.worker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.ProcessResult;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationWorkerProcessor {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogEvaluationCoreService core;
	private final GrowthLogPersistenceService persistence;
	private final UserRepository userRepository;

	@Transactional
	public ProcessResult process(Long userId, Long growthLogId, String traceId, String processingToken) {

		// 1) 조회
		var growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElse(null);

		if (growthLog == null) {
			return ProcessResult.SKIP_NOT_FOUND;
		}

		// 2) 상태 검증 (명시적 분기)
		GrowthLogStatus status = growthLog.getStatus();

		if (status == GrowthLogStatus.COMPLETED) {
			return ProcessResult.SKIP_ALREADY_COMPLETED;
		}

		if (status != GrowthLogStatus.PROCESSING) {
			// PENDING, FAILED 등 예상치 못한 상태
			return ProcessResult.SKIP_NOT_PROCESSING;
		}

		// 3) 토큰 검증
		if (!processingToken.equals(growthLog.getProcessingToken())) {
			return ProcessResult.SKIP_TOKEN_MISMATCH;
		}

		// 4) Lock 획득 (apply 중복 방지)
		int locked = growthLogRepository.acquireApplyLock(userId, growthLogId, processingToken);
		if (locked == 0) {
			// 이미 다른 worker가 적용 중이거나 appliedProcessingToken이 세팅됨
			return ProcessResult.SKIP_ALREADY_APPLYING;
		}

		// 5) 유저 정보 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		Integer userLevel = user.getLevel();
		Long jobId = user.getJob() != null ? user.getJob().getId() : null;

		// 6) 평가 수행
		String content = growthLog.getContent();
		if (content == null || content.isBlank()) {
			content = "(내용 없음)";
		}

		var ctx = core.buildContext(userId, content);
		var evaluated = core.evaluate(userId, jobId, userLevel, ctx);

		// 7) 완료 반영
		persistence.completeGrowthLogAfterProcessing(
			userId,
			growthLogId,
			evaluated.normalized(),
			evaluated.totalDelta()
		);

		// 8) 토큰 정리
		growthLogRepository.clearProcessingTokenIfMatch(
			userId, growthLogId, processingToken, GrowthLogStatus.COMPLETED
		);

		return ProcessResult.COMPLETED;
	}

	@Transactional
	public void markFailedIfProcessing(Long userId, Long growthLogId, String processingToken) {
		// 원자적으로 처리 (조회 없이 바로 update)
		growthLogRepository.updateStatusIfMatchAndToken(
			userId, growthLogId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED, processingToken
		);
	}

}