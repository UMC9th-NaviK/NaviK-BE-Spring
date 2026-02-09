package navik.domain.kpi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.kpi.entity.KpiScore;
import navik.domain.kpi.repository.projection.KpiCardPercentileView;

public interface KpiScoreRepository extends JpaRepository<KpiScore, Long> {

	List<KpiScore> findAllByUserIdAndKpiCard_IdIn(Long userId, List<Long> kpiCardIds);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		    update KpiScore ks
		       set ks.score = ks.score + :delta,
		           ks.updatedAt = CURRENT_TIMESTAMP
		     where ks.user.id = :userId
		       and ks.kpiCard.id = :kpiCardId
		""")
	int incrementScore(@Param("userId") Long userId,
		@Param("kpiCardId") Long kpiCardId,
		@Param("delta") int delta);

	Optional<KpiScore> findByUserIdAndKpiCard_Id(Long userId, Long kpiCardId);

	@Query("""
		    select ks
		      from KpiScore ks
		      join fetch ks.kpiCard
		     where ks.user.id = :userId
		     order by ks.score desc, ks.id desc
		""")
	List<KpiScore> findTopByUserIdWithCard(@Param("userId") Long userId, Pageable pageable);

	@Query("""
		    select ks
		      from KpiScore ks
		      join fetch ks.kpiCard
		     where ks.user.id = :userId
		     order by ks.score asc, ks.id asc
		""")
	List<KpiScore> findBottomByUserIdWithCard(@Param("userId") Long userId, Pageable pageable);

	// Score 백분위
	@Query(value = """
		    with ranked as (
		        select
		            user_id,
		            score,
		            cume_dist() over (
		                partition by kpi_card_id
		                order by score
		            ) as cd
		        from kpi_scores
		        where kpi_card_id = :kpiCardId
		    )
		    select
		        r.score as score,
		        cast(round((1 - r.cd) * 100) as int) as topPercent,
		        cast(round(r.cd * 100) as int) as bottomPercent
		    from ranked r
		    where r.user_id = :userId
		""", nativeQuery = true)
	KpiCardPercentileView findMyPercentile(
		@Param("userId") Long userId,
		@Param("kpiCardId") Long kpiCardId
	);

	// 전체 Score 합계 반환
	@Query("""
			select coalesce(sum(ks.score), 0L)
			  from KpiScore ks
			 where ks.user.id = :userId
		""")
	Long sumTotalScore(@Param("userId") Long userId);

}
