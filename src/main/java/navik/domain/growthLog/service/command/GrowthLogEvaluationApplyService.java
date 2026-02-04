package navik.domain.growthLog.service.command;

import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Slf4j
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

		if (growthLog.getStatus() == GrowthLogStatus.PROCESSING
			&& java.util.Objects.equals(token, growthLog.getProcessingToken()))
			return;

		if (growthLog.getStatus() == GrowthLogStatus.COMPLETED)
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

		int locked = growthLogRepository.acquireApplyLock(userId, growthLogId, token);
		if (locked == 0) {
			return;
		}

		if (!token.equals(growthLog.getProcessingToken())) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.PROCESSING_TOKEN_MISMATCH);
		}

		// totalDelta는 Spring에서 재계산
		int totalDelta = req.kpis().stream().mapToInt(GrowthLogInternalApplyEvaluationRequest.KpiDelta::delta).sum();
		List<GrowthLogEvaluationResult.AbilityResult> abilities = convertAbilities(req.abilities());

		GrowthLogEvaluationResult normalized =
			new GrowthLogEvaluationResult(
				req.title(),
				req.content(),
				req.kpis().stream()
					.map(k -> new GrowthLogEvaluationResult.KpiDelta(k.kpiCardId(), k.delta()))
					.toList(),
				abilities
			);

		List<GrowthLogEvaluationResult.KpiDelta> kpis =
			normalized.kpis(); // 동일 리스트 재사용

		persistence.completeGrowthLogAfterProcessing(
			userId,
			growthLogId,
			normalized,
			totalDelta,
			kpis,
			abilities
		);

		growthLogRepository.clearProcessingTokenIfMatch(userId, growthLogId, token, GrowthLogStatus.COMPLETED);
	}

	private List<GrowthLogEvaluationResult.AbilityResult> convertAbilities(
		List<GrowthLogInternalApplyEvaluationRequest.AbilityDelta> abilities
	) {
		if (abilities == null || abilities.isEmpty()) {
			return List.of();
		}

		return abilities.stream()
			.filter(a -> {
				if (a == null || a.content() == null || a.content().isBlank()) {
					log.warn("Invalid ability content filtered: {}", a);
					return false;
				}
				if (a.embedding() == null || a.embedding().length != 1536) {
					log.warn("Invalid ability embedding dimension filtered: content={}, dimension={}",
						a.content(),
						a.embedding() == null ? "null" : a.embedding().length);
					return false;
				}
				return true;
			})
			.map(a -> new GrowthLogEvaluationResult.AbilityResult(a.content().trim(), a.embedding()))
			.toList();
	}
}
