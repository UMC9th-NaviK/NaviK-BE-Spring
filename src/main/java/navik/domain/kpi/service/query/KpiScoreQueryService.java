package navik.domain.kpi.service.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class KpiScoreQueryService {

	private final KpiScoreRepository kpiScoreRepository;
	private final GrowthLogRepository growthLogRepository;

	// 상위 3개 KPIScore 조회해서 KPICard 반환
	public List<KpiCardResponseDTO.GridItem> getTop3KpiCards(Long userId) {
		return kpiScoreRepository.findTopByUserIdWithCard(userId, PageRequest.of(0, 3)).stream()
			.map(ks -> new KpiCardResponseDTO.GridItem(
				ks.getKpiCard().getId(),
				ks.getKpiCard().getName()
			))
			.toList();
	}

	// 하위 3개 KPIScore 조회해서 KPICard 반환
	public List<KpiCardResponseDTO.GridItem> getBottom3KpiCards(Long userId) {
		return kpiScoreRepository.findBottomByUserIdWithCard(userId, PageRequest.of(0, 3)).stream()
			.map(ks -> new KpiCardResponseDTO.GridItem(
				ks.getKpiCard().getId(),
				ks.getKpiCard().getName()
			))
			.toList();
	}

	// 해당 KPIScore 백분위 반환
	public KpiScoreResponseDTO.Percentile getMyPercentile(
		Long userId,
		Long kpiCardId
	) {
		var view = kpiScoreRepository.findMyPercentile(userId, kpiCardId);

		if (view == null) {
			throw new GeneralExceptionHandler(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND);
		}

		return new KpiScoreResponseDTO.Percentile(
			kpiCardId,
			view.getScore(),
			view.getTopPercent(),
			view.getBottomPercent()
		);
	}

	// 전월 대비 점수 증감 비율 조회
	public KpiScoreResponseDTO.MonthlyTotalScoreChange getMonthlyChangeRate(Long userId) {
		YearMonth currentYm = YearMonth.now();
		LocalDate currentMonthStart = currentYm.atDay(1);

		LocalDateTime start = currentMonthStart.atStartOfDay();
		LocalDateTime end = currentYm.plusMonths(1).atDay(1).atStartOfDay();

		// 현재 전체 누적 점수
		long currentTotalScore = kpiScoreRepository.sumTotalScore(userId);

		// 이번 달 증가분
		List<Object[]> rows = growthLogRepository.sumByMonth(userId, null, start, end);

		int deltaThisMonth = 0;
		for (Object[] row : rows) {
			int sumScore = extractSumScore(row[1]);
			deltaThisMonth += sumScore;
		}

		//전월 말 누적 점수 = 현재 누적 - 이번 달 증가분
		long prevTotalScore = currentTotalScore - deltaThisMonth;

		Double changeRate = calculateChangeRate(currentTotalScore, prevTotalScore);

		return new KpiScoreResponseDTO.MonthlyTotalScoreChange(
			currentYm.getYear(),
			currentYm.getMonthValue(),
			currentTotalScore,
			prevTotalScore,
			changeRate
		);
	}

	private int extractSumScore(Object value) {
		if (value == null) {
			return 0;
		}
		return ((Number)value).intValue();
	}

	private Double calculateChangeRate(long current, long prev) {
		if (prev == 0) {
			return null;
		}
		double rate = ((double)(current - prev) / (double)prev) * 100.0;
		return Math.round(rate * 10) / 10.0;
	}

}
