package navik.domain.kpi.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.job.repository.JobRepository;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.enums.KpiCardType;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.users.exception.code.JobErrorCode;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiCardQueryService {

	private final KpiCardRepository kpiCardRepository;
	private final JobRepository jobRepository;
	private final UserRepository userRepository;

	// 유저 직무 기반 KPI 카드 조회
	public List<GridItem> getAllCardsByUser(Long userId) {
		Long jobId = userRepository.findJobIdByUserId(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(JobErrorCode.JOB_NOT_ASSIGNED));

		return kpiCardRepository.findGridByJobId(jobId).stream()
			.map(v -> new KpiCardResponseDTO.GridItem(v.getId(), v.getName(), v.getImageUrl()))
			.toList();
	}

	// 직무 ID 기반 KPI 카드 조회
	public List<GridItem> getAllCardsByJob(Long jobId) {

		jobRepository.findById(jobId)
			.orElseThrow(() -> new GeneralExceptionHandler(JobErrorCode.JOB_NOT_FOUND));

		return kpiCardRepository.findGridByJobId(jobId).stream()
			.map(v -> new KpiCardResponseDTO.GridItem(v.getId(), v.getName(), v.getImageUrl()))
			.toList();
	}

	// KPI 카드 강점/약점 별 상세 내용 반환
	public KpiCardResponseDTO.Detail getCardDetail(Long cardId, KpiCardType type) {

		KpiCard card = kpiCardRepository.findById(cardId)
			.orElseThrow(() -> new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

		return new KpiCardResponseDTO.Detail(
			card.getId(),
			card.getName(),
			type.toContent(card),
			card.getImageUrl()
		);
	}

	// KPI 카드 전체 상세 내용 반환
	public KpiCardResponseDTO.AllDetail getCardAllDetail(Long cardId) {
		KpiCard card = kpiCardRepository.findById(cardId)
			.orElseThrow(() -> new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

		return new KpiCardResponseDTO.AllDetail(
			card.getId(),
			card.getName(),
			new KpiCardResponseDTO.Content(card.getStrongTitle(), card.getStrongContent()),
			new KpiCardResponseDTO.Content(card.getWeakTitle(), card.getWeakContent()),
			card.getImageUrl()
		);
	}
}

