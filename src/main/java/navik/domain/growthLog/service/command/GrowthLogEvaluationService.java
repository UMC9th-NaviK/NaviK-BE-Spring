package navik.domain.growthLog.service.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.GrowthLogAiClient;
import navik.domain.growthLog.ai.RetryRateLimiter;
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
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationService {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogKpiLinkRepository growthLogKpiLinkRepository;
	private final KpiCardRepository kpiCardRepository;

	private final GrowthLogAiClient growthLogAiClient;
	private final GrowthLogPersistenceService growthLogPersistenceService;

	private final RetryRateLimiter retryRateLimiter;

	public Long create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {

		GrowthLogEvaluationContext context = buildContext(userId, req);

		try {
			// 1) AI 평가
			GrowthLogEvaluationResult aiResult =
				growthLogAiClient.evaluateUserInput(userId, context);

			// 2) 정규화
			GrowthLogEvaluationResult normalized = normalize(aiResult);

			// 3) KPI merge + 검증
			List<GrowthLogEvaluationResult.KpiDelta> kpis =
				mergeSameKpi(normalized.kpis());
			validateKpisExist(kpis);

			// 4) totalDelta
			int totalDelta =
				kpis.stream().mapToInt(GrowthLogEvaluationResult.KpiDelta::delta).sum();

			// 5) 정상 저장
			return growthLogPersistenceService.persist(
				userId,
				normalized,
				totalDelta,
				kpis
			);

		} catch (Exception e) {
			// Soft Fail
			return growthLogPersistenceService.persistFailed(
				userId,
				safe(req.title()),
				safe(req.content())
			);
		}
	}

	public GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId) {

		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(
				GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND
			));

		// 재시도 가능 조건
		if (growthLog.getType() != GrowthType.USER_INPUT) {
			throw new GeneralExceptionHandler(
				GrowthLogErrorCode.INVALID_GROWTH_LOG_TYPE
			);
		}

		if (growthLog.getStatus() != GrowthLogStatus.FAILED) {
			throw new GeneralExceptionHandler(
				GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS
			);
		}

		String key = "growthLogRetry:" + userId + ":" + growthLogId;
		if (!retryRateLimiter.tryAcquire(key, 3)) {
			throw new GeneralExceptionHandler(
				GrowthLogErrorCode.GROWTH_LOG_RETRY_LIMIT_EXCEEDED
			);
		}

		GrowthLogEvaluationContext context =
			buildContextForRetry(userId, growthLog);

		try {
			GrowthLogEvaluationResult aiResult =
				growthLogAiClient.evaluateUserInput(userId, context);

			GrowthLogEvaluationResult normalized = normalize(aiResult);

			List<GrowthLogEvaluationResult.KpiDelta> kpis =
				mergeSameKpi(normalized.kpis());
			validateKpisExist(kpis);

			int totalDelta = kpis.stream()
				.mapToInt(GrowthLogEvaluationResult.KpiDelta::delta)
				.sum();

			growthLogPersistenceService.applyRetryResult(
				userId,
				growthLogId,
				normalized,
				totalDelta,
				kpis
			);

			return new GrowthLogResponseDTO.RetryResult(
				growthLogId,
				GrowthLogStatus.COMPLETED
			);

		} catch (Exception e) {
			// 재시도 실패 → 상태 유지
			return new GrowthLogResponseDTO.RetryResult(
				growthLogId,
				GrowthLogStatus.FAILED
			);
		}
	}

	private GrowthLogEvaluationContext buildContext(Long userId, GrowthLogRequestDTO.CreateUserInput req) {
		// TODO: resumeText는 실제 이력서/포트폴리오 텍스트로 교체
		String resumeText = "";

		// 최근 성장로그 N개
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

		// 최근 로그들의 KPI 변화 이력
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
			safe(req.title()),
			safe(req.content())
		);
	}

	private GrowthLogEvaluationContext buildContextForRetry(Long userId, GrowthLog failedLog) {
		String resumeText = "";

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
			: growthLogKpiLinkRepository.findByGrowthLogIdIn(recentIds); // fetch join 적용된 메서드

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
			safe(failedLog.getTitle()),
			safe(failedLog.getContent())
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

		// 혹시 모를 중복/방어를 위해 distinct
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
}
