package navik.domain.growthLog.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
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

	public Long createFeedback(Long kpiCardId, Integer delta, String content) {
		KpiCard kpiCard = findKpiCard(kpiCardId);

		GrowthLog growthLog = GrowthLog.builder()
			.type(GrowthType.FEEDBACK)
			.title(FEEDBACK_TITLE)
			.content(content == null ? "" : content.trim())
			.totalDelta(delta)
			.build();

		growthLog.addKpiLink(
			GrowthLogKpiLink.builder()
				.kpiCard(kpiCard)
				.delta(delta)
				.build()
		);

		return growthLogRepository.save(growthLog).getId();
	}

	public Long createPortfolio(Long kpiCardId, Integer delta) {
		KpiCard kpiCard = findKpiCard(kpiCardId);

		GrowthLog growthLog = GrowthLog.builder()
			.type(GrowthType.PORTFOLIO)
			.title(String.format(PORTFOLIO_TITLE_FORMAT, delta))
			.content(String.format(PORTFOLIO_CONTENT_FORMAT, delta))
			.totalDelta(delta)
			.build();

		growthLog.addKpiLink(
			GrowthLogKpiLink.builder()
				.kpiCard(kpiCard)
				.delta(delta)
				.build()
		);

		return growthLogRepository.save(growthLog).getId();
	}

	private KpiCard findKpiCard(Long kpiCardId) {
		return kpiCardRepository.findById(kpiCardId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.KPI_CARD_NOT_FOUND));
	}
}
