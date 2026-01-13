package navik.domain.kpi.repository;

import java.util.List;
import java.util.Optional;
import navik.domain.kpi.entity.KpiScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface KpiScoreRepository extends JpaRepository<KpiScore, Long> {

    List<KpiScore> findAllByUserIdAndKpiCard_IdIn(Long userId, List<Long> kpiCardIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update KpiScore ks
           set ks.score = ks.score + :delta
         where ks.user.id = :userId
           and ks.kpiCard.id = :kpiCardId
    """)
    int incrementScore(Long userId, Long kpiCardId, int delta);

    Optional<KpiScore> findByUserIdAndKpiCard_Id(Long userId, Long kpiCardId);

}
