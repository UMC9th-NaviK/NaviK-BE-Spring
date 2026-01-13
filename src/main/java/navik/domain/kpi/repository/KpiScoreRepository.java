package navik.domain.kpi.repository;

import java.util.List;
import navik.domain.kpi.entity.KpiScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiScoreRepository extends JpaRepository<KpiScore, Long> {

    List<KpiScore> findAllByUserIdAndKpiCard_IdIn(Long userId, List<Long> kpiCardIds);

}
