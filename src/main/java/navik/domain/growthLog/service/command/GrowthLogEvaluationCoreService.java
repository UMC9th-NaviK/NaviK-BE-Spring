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
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogKpiLinkRepository;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationCoreService {

	private static final int EMBEDDING_DIM = 1536;
	private static final int CONTENT_LOG_MAX = 30;

	private final GrowthLogRepository growthLogRepository;
	private final GrowthLogKpiLinkRepository growthLogKpiLinkRepository;
	private final KpiCardRepository kpiCardRepository;
	private final PortfolioRepository portfolioRepository;
	private final UserRepository userRepository;
	private final GrowthLogAiClient growthLogAiClient;

	public GrowthLogEvaluationContext buildContext(Long userId, String inputContent) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		Integer userLevel = user.getLevel();
		Long jobId = user.getJob() != null ? user.getJob().getId() : null;

		String resumeText = portfolioRepository
			.findTopByUserIdOrderByCreatedAtDesc(userId)
			.map(this::buildResumeText)
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

		List<GrowthLogEvaluationResult.AbilityResult> abilities = normalizeAbilities(base.abilities());

		int totalDelta = kpis.stream()
			.mapToInt(GrowthLogEvaluationResult.KpiDelta::delta)
			.sum();

		GrowthLogEvaluationResult normalized =
			new GrowthLogEvaluationResult(base.title(), base.content(), kpis, abilities);

		return new Evaluated(normalized, totalDelta);
	}

	private GrowthLogEvaluationResult normalize(GrowthLogEvaluationResult r) {
		if (r == null) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.AI_EVALUATION_FAILED);
		}

		String title = (r.title() == null) ? "" : r.title().trim();
		String content = (r.content() == null) ? "" : r.content().trim();
		List<GrowthLogEvaluationResult.KpiDelta> kpis = (r.kpis() == null) ? List.of() : r.kpis();
		List<GrowthLogEvaluationResult.AbilityResult> abilities = (r.abilities() == null) ? List.of() : r.abilities();

		if (title.isBlank())
			title = "사용자 입력 성장 기록";
		if (content.isBlank())
			content = "(내용 없음)";

		return new GrowthLogEvaluationResult(title, content, kpis, abilities);
	}

	private List<GrowthLogEvaluationResult.AbilityResult> normalizeAbilities(
		List<GrowthLogEvaluationResult.AbilityResult> abilities
	) {
		if (abilities == null || abilities.isEmpty()) {
			return List.of();
		}

		return abilities.stream()
			.filter(a -> {
				if (a == null) {
					log.warn("Invalid ability filtered: null");
					return false;
				}

				String content = a.content();
				if (content == null || content.isBlank()) {
					log.warn("Invalid ability content filtered: blank");
					return false;
				}

				float[] embedding = a.embedding();
				if (embedding == null || embedding.length != EMBEDDING_DIM) {
					log.warn(
						"Invalid ability embedding dimension filtered: content={}, dimension={}",
						abbreviate(content.trim()),
						embedding == null ? "null" : embedding.length
					);
					return false;
				}

				// (선택) NaN / Infinity 방어
				for (float v : embedding) {
					if (!Float.isFinite(v)) {
						log.warn(
							"Invalid ability embedding value filtered: content={}",
							abbreviate(content.trim())
						);
						return false;
					}
				}

				return true;
			})
			.map(a -> new GrowthLogEvaluationResult.AbilityResult(
				a.content().trim(),
				a.embedding()
			))
			.toList();
	}

	private String abbreviate(String s) {
		return (s.length() <= GrowthLogEvaluationCoreService.CONTENT_LOG_MAX) ? s : s.substring(0,
			GrowthLogEvaluationCoreService.CONTENT_LOG_MAX) + "...";
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

	private String buildResumeText(Portfolio p) {
		return """
			[포트폴리오 내용]
			%s
			""".formatted(
			safe(p.getContent())
		);
	}

}
