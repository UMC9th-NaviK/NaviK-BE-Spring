package navik.domain.growthLog.service.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.client.GrowthLogAiClient;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.GrowthLogEvaluationContext;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.PastGrowthLog;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.PastKpiDelta;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogKpiLinkRepository;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationService {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogKpiLinkRepository growthLogKpiLinkRepository;
	private final KpiCardRepository kpiCardRepository;
	private final PortfolioRepository portfolioRepository;

	private final GrowthLogAiClient growthLogAiClient;
	private final GrowthLogPersistenceService growthLogPersistenceService;

	private final RetryRateLimiter retryRateLimiter;

	public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {

		String inputContent = safe(req.content());
		GrowthLogEvaluationContext context = buildContext(userId, inputContent);

		try {
			Evaluated evaluated = evaluateGrowthLog(userId, context);

			Long id = growthLogPersistenceService.saveUserInputLog(
				userId,
				evaluated.normalized(),
				evaluated.totalDelta(),
				evaluated.kpis()
			);

			return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.COMPLETED);

		} catch (Exception e) {
			Long id = growthLogPersistenceService.saveFailedUserInputLog(userId, inputContent);
			return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.FAILED);
		}
	}

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

		// 동시 재시도 차단: FAILED -> PENDING 원자적 전환
		int acquired = growthLogRepository.updateStatusIfMatch(
			userId,
			growthLogId,
			GrowthLogStatus.FAILED,
			GrowthLogStatus.PENDING
		);

		if (acquired == 0) {
			// 누군가가 이미 재시도를 시작했거나, 상태가 FAILED가 아님
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		GrowthLogEvaluationContext context = buildContext(userId, safe(growthLog.getContent()));

		try {
			Evaluated evaluated = evaluateGrowthLog(userId, context);

			growthLogPersistenceService.updateGrowthLogAfterRetry(
				userId,
				growthLogId,
				evaluated.normalized(),
				evaluated.totalDelta(),
				evaluated.kpis()
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

	private Evaluated evaluateGrowthLog(Long userId, GrowthLogEvaluationContext context) {
		GrowthLogEvaluationResult aiResult = growthLogAiClient.evaluateUserInput(userId, context);

		GrowthLogEvaluationResult normalized = normalize(aiResult);

		List<GrowthLogEvaluationResult.KpiDelta> kpis = mergeSameKpi(normalized.kpis());
		validateKpisExist(kpis);

		int totalDelta = kpis.stream()
			.mapToInt(GrowthLogEvaluationResult.KpiDelta::delta)
			.sum();

		return new Evaluated(normalized, kpis, totalDelta);
	}

	private GrowthLogEvaluationContext buildContext(Long userId, String inputContent) {
		String resumeText = portfolioRepository
			.findTopByUserIdOrderByCreatedAtDesc(userId)
			.map(p -> buildResumeText(p))
			.orElse("포트폴리오 정보 없음");

		List<GrowthLog> recentLogs = growthLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);

		List<PastGrowthLog> recentGrowthLogs = recentLogs.stream()
			.map(gl -> new PastGrowthLog(
				gl.getId(),
				gl.getType().name(),
				gl.getTitle(),
				gl.getContent(),
				gl.getTotalDelta(),
				gl.getCreatedAt()
			))
			.toList();

		List<Long> recentIds = recentLogs.stream().map(GrowthLog::getId).toList();
		List<GrowthLogKpiLink> links = recentIds.isEmpty()
			? List.of()
			: growthLogKpiLinkRepository.findByGrowthLogIdIn(recentIds);

		List<PastKpiDelta> recentKpiDeltas = links.stream()
			.map(l -> new PastKpiDelta(
				l.getGrowthLog().getId(),
				l.getKpiCard().getId(),
				l.getDelta()
			))
			.toList();

		return new GrowthLogEvaluationContext(
			resumeText,
			recentGrowthLogs,
			recentKpiDeltas,
			inputContent
		);
	}

	private GrowthLogEvaluationResult normalize(GrowthLogEvaluationResult r) {
		if (r == null) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.AI_EVALUATION_FAILED);
		}

		String title = (r.title() == null) ? "" : r.title().trim();
		String content = (r.content() == null) ? "" : r.content().trim();
		List<GrowthLogEvaluationResult.KpiDelta> kpis = (r.kpis() == null) ? List.of() : r.kpis();

		if (title.isBlank())
			title = "사용자 입력 성장 기록";
		if (content.isBlank())
			content = "(내용 없음)";

		return new GrowthLogEvaluationResult(title, content, kpis);
	}

	private List<GrowthLogEvaluationResult.KpiDelta> mergeSameKpi(List<GrowthLogEvaluationResult.KpiDelta> kpis) {
		Map<Long, Integer> merged = new LinkedHashMap<>();
		for (var kd : kpis) {
			if (kd == null || kd.kpiCardId() == null || kd.delta() == null)
				continue;
			merged.merge(kd.kpiCardId(), kd.delta(), Integer::sum);
		}
		return merged.entrySet().stream()
			.map(e -> new GrowthLogEvaluationResult.KpiDelta(e.getKey(), e.getValue()))
			.toList();
	}

	private void validateKpisExist(List<GrowthLogEvaluationResult.KpiDelta> kpis) {
		if (kpis.isEmpty())
			return;

		List<Long> ids = kpis.stream()
			.map(GrowthLogEvaluationResult.KpiDelta::kpiCardId)
			.distinct()
			.toList();

		long count = kpiCardRepository.countByIdIn(ids);
		if (count != ids.size()) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.KPI_CARD_NOT_FOUND);
		}
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}

	private record Evaluated(
		GrowthLogEvaluationResult normalized,
		List<GrowthLogEvaluationResult.KpiDelta> kpis,
		int totalDelta
	) {
	}

	private String buildResumeText(Portfolio p) {
		return """
			[포트폴리오 제목]
			%s
			
			[포트폴리오 내용]
			%s
			""".formatted(
			safe(p.getTitle()),
			safe(p.getContent())
		);
	}

}
