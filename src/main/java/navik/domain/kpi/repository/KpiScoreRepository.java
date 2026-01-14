package navik.domain.kpi.repository;

import java.util.List;
import java.util.Optional;
import navik.domain.kpi.entity.KpiScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 상위 3개
    @Query("""
        select ks
          from KpiScore ks
          join fetch ks.kpiCard kc
         where ks.user.id = :userId
         order by ks.score desc, ks.id desc
    """)
    List<KpiScore> findTop3ByUserIdWithCard(@Param("userId") Long userId);

    // 하위 3개
    @Query("""
        select ks
          from KpiScore ks
          join fetch ks.kpiCard
         where ks.user.id = :userId
         order by ks.score asc, ks.id asc
    """)
    List<KpiScore> findBottom3ByUserIdWithCard(@Param("userId") Long userId);
}
