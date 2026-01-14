package navik.domain.kpi.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class KpiScoreQueryService {

	private final KpiScoreRepository kpiScoreRepository;

	public List<GridItem> getTop3KpiCards(Long userId) {
		return kpiScoreRepository.findTop3ByUserIdWithCard(userId).stream()
			.map(ks -> new KpiCardResponseDTO.GridItem(
				ks.getKpiCard().getId(),
				ks.getKpiCard().getName()
			))
			.toList();
	}

	public List<KpiCardResponseDTO.GridItem> getBottom3KpiCards(Long userId) {
		return kpiScoreRepository.findBottom3ByUserIdWithCard(userId).stream()
			.map(ks -> new KpiCardResponseDTO.GridItem(
				ks.getKpiCard().getId(),
				ks.getKpiCard().getName()
			))
			.toList();
	}

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
