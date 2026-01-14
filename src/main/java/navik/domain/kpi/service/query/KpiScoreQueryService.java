package navik.domain.kpi.service.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.repository.KpiScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //TODO: KPI Score 전체 순위 조회

}
