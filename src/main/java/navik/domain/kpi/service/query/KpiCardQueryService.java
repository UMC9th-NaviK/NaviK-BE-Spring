package navik.domain.kpi.service.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import navik.domain.job.repository.JobRepository;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
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

    //TODO : KPICard Detail 조회
    //핵심 역량인지 극복역량인지에 따라 조건에 달리
    //직군에 따라서도
}
