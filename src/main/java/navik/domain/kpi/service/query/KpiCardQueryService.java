package navik.domain.kpi.service.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import navik.domain.job.repository.JobRepository;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.enums.KpiCardType;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.users.exception.code.JobErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiCardQueryService {

    private final KpiCardRepository kpiCardRepository;
    private final JobRepository jobRepository;

    public List<GridItem> getAllCardsByJob(Long jobId) {

        jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralExceptionHandler(JobErrorCode.JOB_NOT_FOUND));

        return kpiCardRepository.findGridByJobId(jobId).stream()
                .map(v -> new KpiCardResponseDTO.GridItem(v.getId(), v.getName()))
                .toList();
    }

    public KpiCardResponseDTO.Detail getCardDetail(Long cardId, KpiCardType type) {

        KpiCard card = kpiCardRepository.findById(cardId)
                .orElseThrow(() -> new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

        return new KpiCardResponseDTO.Detail(
                card.getId(),
                card.getName(),
                type.toContent(card)
        );
    }

    public KpiCardResponseDTO.AllDetail getCardAllDetail(Long cardId) {
        KpiCard card = kpiCardRepository.findById(cardId)
                .orElseThrow(() -> new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

        return new KpiCardResponseDTO.AllDetail(
                card.getId(),
                card.getName(),
                new KpiCardResponseDTO.Content(card.getStrongTitle(), card.getStrongContent()),
                new KpiCardResponseDTO.Content(card.getWeakTitle(), card.getWeakContent())
        );
    }
}

