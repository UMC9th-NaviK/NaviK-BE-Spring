package navik.domain.kpi.service.query;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
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
}
