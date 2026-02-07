package navik.domain.growthLog.service.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.ai.client.GrowthLogAiClient;
import navik.domain.growthLog.dto.internal.Evaluated;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.GrowthLogEvaluationContext;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.PastGrowthLog;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.PastKpiDelta;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogKpiLinkRepository;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationCoreService {

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogKpiLinkRepository growthLogKpiLinkRepository;
	private final KpiCardRepository kpiCardRepository;
	private final PortfolioRepository portfolioRepository;
	private final UserRepository userRepository;
	private final GrowthLogAiClient growthLogAiClient;

	public GrowthLogEvaluationContext buildContext(Long userId, String inputContent) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		Integer userLevel = user.getLevel();
		Long jobId = user.getJob() != null ? user.getJob().getId() : null;

		String resumeText = portfolioRepository
			.findTopByUserIdOrderByCreatedAtDesc(userId)
			.map(this::buildResumeText)
			.orElse("포트폴리오 정보 없음");

		List<GrowthLog> recentLogs = growthLogRepository.findTop20ByUserIdAndStatusOrderByCreatedAtDesc(userId,
			GrowthLogStatus.COMPLETED);

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
			jobId,
			userLevel,
			resumeText,
			recentGrowthLogs,
			recentKpiDeltas,
			safe(inputContent)
		);
	}

	public Evaluated evaluate(Long userId, GrowthLogEvaluationContext context) {
		GrowthLogEvaluationResult aiResult = growthLogAiClient.evaluateUserInput(userId, context);

		GrowthLogEvaluationResult base = normalize(aiResult);

		List<GrowthLogEvaluationResult.KpiDelta> kpis = mergeSameKpi(base.kpis());
		validateKpisExist(kpis);

		int totalDelta = kpis.stream()
			.mapToInt(GrowthLogEvaluationResult.KpiDelta::delta)
			.sum();

		GrowthLogEvaluationResult normalized =
			new GrowthLogEvaluationResult(base.title(), base.content(), kpis);

		return new Evaluated(normalized, totalDelta);
	}

	private GrowthLogEvaluationResult normalize(GrowthLogEvaluationResult r) {
		if (r == null) {
			throw new GeneralException(GrowthLogErrorCode.AI_EVALUATION_FAILED);
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
			throw new GeneralException(GrowthLogErrorCode.KPI_CARD_NOT_FOUND);
		}
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}

	private String buildResumeText(Portfolio p) {
		return """
			[포트폴리오 내용]
			%s
			""".formatted(
			safe(p.getContent())
		);
	}

}
