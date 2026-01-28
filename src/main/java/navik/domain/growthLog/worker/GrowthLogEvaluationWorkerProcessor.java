package navik.domain.growthLog.worker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationWorkerProcessor {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogEvaluationCoreService core;
	private final GrowthLogPersistenceService persistence;

	@Transactional
	public ProcessResult process(Long userId, Long growthLogId, String traceId) {

		// 1) 선점: PENDING -> PROCESSING
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.PENDING,
			GrowthLogStatus.PROCESSING
		);

		if (acquired == 0) {
			return ProcessResult.SKIP_NOT_PENDING;
		}

		// 2) 입력(content) 조회
		String content = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.map(gl -> gl.getContent())
			.orElse("(내용 없음)");

		// 3) 평가 수행
		var ctx = core.buildContext(userId, content);
		var evaluated = core.evaluate(userId, ctx);

		// 4) 완료 반영 (applyEvaluation()에서 COMPLETED 처리)
		persistence.completeGrowthLogAfterProcessing(
			userId,
			growthLogId,
			evaluated.normalized(),
			evaluated.totalDelta(),
			evaluated.kpis()
		);

		return ProcessResult.COMPLETED;
	}

	@Transactional
	public void markFailedIfProcessing(Long userId, Long growthLogId) {
		growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.PROCESSING,
			GrowthLogStatus.FAILED
		);
	}

	public enum ProcessResult {
		COMPLETED,
		SKIP_NOT_PENDING
	}
}
