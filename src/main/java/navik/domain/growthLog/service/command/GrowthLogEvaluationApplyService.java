package navik.domain.growthLog.service.command;

import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.ability.normalizer.AbilityNormalizer;
import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationApplyService {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogPersistenceService persistence;
	private final AbilityNormalizer abilityNormalizer;

	@Transactional
	public void startProcessing(Long growthLogId, GrowthLogInternalProcessingStartRequest req) {
		Long userId = req.userId();
		String token = req.processingToken();

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		if (growthLog.getStatus() == GrowthLogStatus.PROCESSING
			&& java.util.Objects.equals(token, growthLog.getProcessingToken()))
			return;

		if (growthLog.getStatus() == GrowthLogStatus.COMPLETED)
			return;

		int updated = growthLogRepository.updateStatusIfMatchAndToken(
			userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
		);

		if (updated == 0) {
			throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}
	}

	@Transactional
	public void applyResult(Long growthLogId, GrowthLogInternalApplyEvaluationRequest req) {
		Long userId = req.userId();
		String token = req.processingToken();

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		// 멱등
		if (growthLog.getStatus() == GrowthLogStatus.COMPLETED)
			return;

		int locked = growthLogRepository.acquireApplyLock(userId, growthLogId, token);
		if (locked == 0) {
			return;
		}

		if (!token.equals(growthLog.getProcessingToken())) {
			throw new GeneralException(GrowthLogErrorCode.PROCESSING_TOKEN_MISMATCH);
		}

		// totalDelta는 Spring에서 재계산
		int totalDelta = req.kpis().stream().mapToInt(GrowthLogInternalApplyEvaluationRequest.KpiDelta::delta).sum();

		List<GrowthLogEvaluationResult.AbilityResult> abilities = abilityNormalizer.normalize(req.abilities());

		GrowthLogEvaluationResult normalized =
			new GrowthLogEvaluationResult(
				req.title(),
				req.content(),
				req.kpis().stream()
					.map(k -> new GrowthLogEvaluationResult.KpiDelta(k.kpiCardId(), k.delta()))
					.toList(),
				abilities
			);

		persistence.completeGrowthLogAfterProcessing(
			userId,
			growthLogId,
			normalized,
			totalDelta
		);

		growthLogRepository.clearProcessingTokenIfMatch(userId, growthLogId, token, GrowthLogStatus.COMPLETED);
	}

}
