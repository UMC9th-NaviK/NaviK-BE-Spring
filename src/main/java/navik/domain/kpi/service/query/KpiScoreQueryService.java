package navik.domain.kpi.service.query;

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
import navik.global.apiPayload.exception.exception.GeneralException;

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
				ks.getKpiCard().getName(),
				ks.getKpiCard().getImageUrl()
			))
			.toList();
	}

	// 하위 3개 KPIScore 조회해서 KPICard 반환
	public List<KpiCardResponseDTO.GridItem> getBottom3KpiCards(Long userId) {
		return kpiScoreRepository.findBottomByUserIdWithCard(userId, PageRequest.of(0, 3)).stream()
			.map(ks -> new KpiCardResponseDTO.GridItem(
				ks.getKpiCard().getId(),
				ks.getKpiCard().getName(),
				ks.getKpiCard().getImageUrl()
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
			throw new GeneralException(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND);
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
		LocalDateTime monthStart = currentYm.atDay(1).atStartOfDay();

		// 현재까지 전체 누적
		long currentTotalScore = growthLogRepository.sumTotalDeltaAll(userId);

		// 전월 말까지 전체 누적
		long prevTotalScore = growthLogRepository.sumTotalDeltaBefore(userId, monthStart);

		// 증가율 계산
		Double changeRate = (prevTotalScore <= 0)
			? null
			: calculateChangeRate(currentTotalScore, prevTotalScore);

		return new KpiScoreResponseDTO.MonthlyTotalScoreChange(
			currentYm.getYear(),
			currentYm.getMonthValue(),
			currentTotalScore,
			prevTotalScore,
			changeRate
		);
	}

	private Double calculateChangeRate(long current, long prev) {
		if (prev == 0) {
			return null;
		}
		double rate = ((double)(current - prev) / (double)prev) * 100.0;
		return Math.round(rate * 10) / 10.0;
	}

}
