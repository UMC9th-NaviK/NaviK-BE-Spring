package navik.domain.growthLog.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.entity.GrowthLog;
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

	public Long createFeedback(Long kpiCardId, Integer score, String content) {
		KpiCard kpiCard = findKpiCard(kpiCardId);

		GrowthLog growthLog = GrowthLog.builder()
			.kpiCard(kpiCard)
			.type(GrowthType.FEEDBACK)
			.title(FEEDBACK_TITLE)
			.content(content == null ? "" : content.trim())
			.score(score)
			.build();

		return growthLogRepository.save(growthLog).getId();
	}

	public Long createPortfolio(Long kpiCardId, Integer score) {
		KpiCard kpiCard = findKpiCard(kpiCardId);

		GrowthLog growthLog = GrowthLog.builder()
			.kpiCard(kpiCard)
			.type(GrowthType.PORTFOLIO)
			.title(String.format(PORTFOLIO_TITLE_FORMAT, score))
			.content(String.format(PORTFOLIO_CONTENT_FORMAT, score))
			.score(score)
			.build();

		return growthLogRepository.save(growthLog).getId();
	}

	// USER_INPUT 로그에 대해 내부(AI/룰) 평가 점수를 반영
	public void applyUserInputResult(Long growthLogId, Long kpiCardId, Integer score) {
		int updated = growthLogRepository.applyUserInputResultOnce(
			growthLogId, kpiCardId, score, GrowthType.USER_INPUT
		);

		if (updated == 0) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND);
		}
	}

	private KpiCard findKpiCard(Long kpiCardId) {
		return kpiCardRepository.findById(kpiCardId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.KPI_CARD_NOT_FOUND));
	}
}
