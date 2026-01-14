package navik.domain.kpi.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.entity.KpiScore;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class KpiScoreIncrementService {

	private final KpiScoreRepository kpiScoreRepository;

	public KpiScoreResponseDTO.Increment incrementKpiScore(Long userId, Long kpiCardId,
		KpiScoreRequestDTO.Increment request) {
		int delta = (request == null || request.delta() == null) ? 1 : request.delta();

		int updatedRows = kpiScoreRepository.incrementScore(userId, kpiCardId, delta);
		if (updatedRows == 0) {
			throw new GeneralExceptionHandler(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND);
		}

		KpiScore score = kpiScoreRepository.findByUserIdAndKpiCard_Id(userId, kpiCardId)
			.orElseThrow(() -> new GeneralExceptionHandler(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND));

		return new KpiScoreResponseDTO.Increment(kpiCardId, score.getScore());
	}
}
