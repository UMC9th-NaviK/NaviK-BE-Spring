package navik.domain.kpi.service.command;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.entity.KpiScore;
import navik.domain.kpi.event.KpiScoreUpdatedEvent;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KpiScoreIncrementService {

	private final KpiScoreRepository kpiScoreRepository;
	private final ApplicationEventPublisher eventPublisher;

	public KpiScoreResponseDTO.Increment incrementKpiScore(Long userId, Long kpiCardId,
		KpiScoreRequestDTO.Increment request) {

		int delta = request.delta();

		int updatedRows = kpiScoreRepository.incrementScore(userId, kpiCardId, delta);
		if (updatedRows == 0) {
			throw new GeneralException(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND);
		}

		KpiScore score = kpiScoreRepository.findByUserIdAndKpiCard_Id(userId, kpiCardId)
			.orElseThrow(() -> new GeneralException(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND));

		return new KpiScoreResponseDTO.Increment(kpiCardId, score.getScore());
	}

	public void incrementInternal(Long userId, Long kpiCardId, int delta) {
		int updatedRows = kpiScoreRepository.incrementScore(userId, kpiCardId, delta);

		if (updatedRows == 0) {
			log.error("[KpiScore] increment failed. userId={}, kpiCardId={}, delta={}",
				userId, kpiCardId, delta);
			throw new GeneralException(KpiScoreErrorCode.KPI_SCORE_NOT_FOUND);
		}

		eventPublisher.publishEvent(new KpiScoreUpdatedEvent(userId));
	}

}
