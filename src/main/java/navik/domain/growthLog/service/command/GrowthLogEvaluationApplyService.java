package navik.domain.growthLog.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationApplyService {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogPersistenceService persistence;

	@Transactional
	public void startProcessing(Long growthLogId, GrowthLogInternalProcessingStartRequest req) {
		Long userId = req.userId();
		String token = req.processingToken();

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		if (growthLog.getStatus() == GrowthLogStatus.COMPLETED)
			return;

		if (growthLog.getStatus() == GrowthLogStatus.PROCESSING
			&& token.equals(growthLog.getProcessingToken()))
			return;

		int updated = growthLogRepository.updateStatusIfMatchAndToken(
			userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
		);

		if (updated == 0) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}
	}

	@Transactional
	public void applyResult(Long growthLogId, GrowthLogInternalApplyEvaluationRequest req) {
		Long userId = req.userId();
		String token = req.processingToken();

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		// 멱등
		if (growthLog.getStatus() == GrowthLogStatus.COMPLETED)
			return;

		// PROCESSING + token 일치 강제
		if (growthLog.getStatus() != GrowthLogStatus.PROCESSING) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_PROCESSING);
		}

		if (!token.equals(growthLog.getProcessingToken())) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.PROCESSING_TOKEN_MISMATCH);
		}

		// totalDelta는 Spring에서 재계산
		int totalDelta = req.kpis().stream().mapToInt(GrowthLogInternalApplyEvaluationRequest.KpiDelta::delta).sum();

		GrowthLogAiResponseDTO.GrowthLogEvaluationResult normalized =
			new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				req.title(),
				req.content(),
				req.kpis().stream()
					.map(k -> new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(k.kpiCardId(), k.delta()))
					.toList()
			);

		List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta> kpis =
			normalized.kpis(); // 동일 리스트 재사용

		persistence.completeGrowthLogAfterProcessing(
			userId,
			growthLogId,
			normalized,
			totalDelta,
			kpis
		);

		growthLogRepository.clearProcessingTokenIfMatch(userId, growthLogId, token);
	}
}
