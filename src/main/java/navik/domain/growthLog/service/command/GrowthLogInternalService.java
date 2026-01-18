package navik.domain.growthLog.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogInternalRequestDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthLogInternalService {

	private final GrowthLogRepository growthLogRepository;
	private final KpiCardRepository kpiCardRepository;

	private static final String FEEDBACK_TITLE = "스터디 피드백";
	private static final String PORTFOLIO_TITLE_FORMAT = "포트폴리오 분석 결과 (%d점)";
	private static final String PORTFOLIO_CONTENT_FORMAT =
		"포트폴리오 분석을 통해 KPI 점수가 초기 설정되었습니다. (점수: %d점)";

	public Long createFeedback(GrowthLogInternalRequestDTO.Create req) {
		return createInternal(req, GrowthType.FEEDBACK, this::feedbackTitleContent);
	}

	public Long createPortfolio(GrowthLogInternalRequestDTO.Create req) {
		return createInternal(req, GrowthType.PORTFOLIO, this::portfolioTitleContent);
	}

	private Long createInternal(
		GrowthLogInternalRequestDTO.Create req,
		GrowthType type,
		TitleContentPolicy policy
	) {
		Map<Long, Integer> mergedDeltas = mergeKpiDeltas(req.kpis());

		if (mergedDeltas.isEmpty()) {
			// 모든 delta가 0으로 합산되어 반영할 변화가 없음
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_REQUEST);
		}

		int totalDelta = mergedDeltas.values().stream().mapToInt(Integer::intValue).sum();
		List<Long> ids = new ArrayList<>(mergedDeltas.keySet());

		Map<Long, KpiCard> kpiCardMap = findKpiCardsAsMap(ids);
		TitleContent tc = policy.make(totalDelta, req.content());

		GrowthLog growthLog = buildGrowthLog(type, totalDelta, tc);
		attachKpiLinks(growthLog, mergedDeltas, kpiCardMap);

		return growthLogRepository.save(growthLog).getId();
	}

	@FunctionalInterface
	private interface TitleContentPolicy {
		TitleContent make(int totalDelta, String content);
	}

	// 공통 로직 메서드

	private Map<Long, Integer> mergeKpiDeltas(List<GrowthLogInternalRequestDTO.KpiDelta> kpis) {
		Map<Long, Integer> merged = kpis.stream()
			.collect(Collectors.groupingBy(
				GrowthLogInternalRequestDTO.KpiDelta::kpiCardId,
				Collectors.summingInt(GrowthLogInternalRequestDTO.KpiDelta::delta)
			));

		// delta == 0 제거
		merged.entrySet().removeIf(e -> e.getValue() == 0);

		return merged;
	}

	private GrowthLog buildGrowthLog(GrowthType type, int totalDelta, TitleContent tc) {
		return GrowthLog.builder()
			.type(type)
			.title(tc.title())
			.content(tc.content())
			.totalDelta(totalDelta)
			.build();
	}

	private void attachKpiLinks(
		GrowthLog growthLog,
		Map<Long, Integer> mergedDeltas,
		Map<Long, KpiCard> kpiCardMap
	) {
		for (var entry : mergedDeltas.entrySet()) {
			KpiCard kpiCard = kpiCardMap.get(entry.getKey());
			if (kpiCard == null) {
				throw new GeneralExceptionHandler(GrowthLogErrorCode.KPI_CARD_NOT_FOUND);
			}

			growthLog.addKpiLink(
				GrowthLogKpiLink.builder()
					.kpiCard(kpiCard)
					.delta(entry.getValue())
					.build()
			);
		}
	}

	private Map<Long, KpiCard> findKpiCardsAsMap(List<Long> ids) {
		List<KpiCard> cards = kpiCardRepository.findAllById(ids);
		if (cards.size() != ids.size()) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.KPI_CARD_NOT_FOUND);
		}
		return cards.stream()
			.collect(Collectors.toMap(KpiCard::getId, c -> c, (a, b) -> a));

	}

	// 타입별 문구 정책

	private TitleContent feedbackTitleContent(int totalDelta, String content) {
		return new TitleContent(FEEDBACK_TITLE, normalizeContent(content));
	}

	private TitleContent portfolioTitleContent(int totalDelta, String content) {
		return new TitleContent(
			String.format(PORTFOLIO_TITLE_FORMAT, totalDelta),
			String.format(PORTFOLIO_CONTENT_FORMAT, totalDelta)
		);
	}

	private String normalizeContent(String content) {
		return content == null ? "" : content.trim();
	}

	private record TitleContent(String title, String content) {
	}
}
