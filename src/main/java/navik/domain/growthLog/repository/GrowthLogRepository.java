package navik.domain.growthLog.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthType;

@Repository
public interface GrowthLogRepository extends JpaRepository<GrowthLog, Long> {

	List<GrowthLog> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("""
		select gl
		  from GrowthLog gl
		 where gl.user.id = :userId
		   and gl.createdAt >= :start
		   and gl.createdAt < :end
		""")
	Page<GrowthLog> findMonthly(
		@Param("userId") Long userId,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		Pageable pageable
	);

	@Query("""
		select gl
		  from GrowthLog gl
		 where gl.user.id = :userId
		   and gl.type = :type
		   and gl.createdAt >= :start
		   and gl.createdAt < :end
		""")
	Page<GrowthLog> findMonthlyByType(
		@Param("userId") Long userId,
		@Param("type") GrowthType type,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		Pageable pageable
	);

	@Query("""
			select min(gl.createdAt)
			  from GrowthLog gl
			 where gl.user.id = :userId
		""")
	Optional<LocalDateTime> findFirstCreatedAt(@Param("userId") Long userId);

	@Query("""
			select min(gl.createdAt)
			  from GrowthLog gl
			 where gl.user.id = :userId
			   and gl.type = :type
		""")
	Optional<LocalDateTime> findFirstCreatedAtByType(
		@Param("userId") Long userId,
		@Param("type") GrowthType type
	);

	// DAY
	@Query(value = """
			select
			  (date_trunc('day', created_at))::date as period_start,
			  sum(score)::int as sum_score
			from growth_logs
			where user_id = :userId
			  and (:type is null or type = cast(:type as varchar))
			  and created_at >= :start
			  and created_at < :end
			group by period_start
			order by period_start
		""", nativeQuery = true)
	List<Object[]> sumByDay(
		@Param("userId") Long userId,
		@Param("type") String type, // GrowthType.name() 넣을 것 (nullable)
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	// WEEK (period_start = 주 시작일(월요일))
	@Query(value = """
			select
			  (date_trunc('week', created_at))::date as period_start,
			  sum(score)::int as sum_score
			from growth_logs
			where user_id = :userId
			  and (:type is null or type = cast(:type as varchar))
			  and created_at >= :start
			  and created_at < :end
			group by period_start
			order by period_start
		""", nativeQuery = true)
	List<Object[]> sumByWeek(
		@Param("userId") Long userId,
		@Param("type") String type,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	// MONTH (period_start = 월 시작일)
	@Query(value = """
			select
			  (date_trunc('month', created_at))::date as period_start,
			  sum(score)::int as sum_score
			from growth_logs
			where user_id = :userId
			  and (:type is null or type = cast(:type as varchar))
			  and created_at >= :start
			  and created_at < :end
			group by period_start
			order by period_start
		""", nativeQuery = true)
	List<Object[]> sumByMonth(
		@Param("userId") Long userId,
		@Param("type") String type,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	Optional<GrowthLog> findByIdAndUserId(Long id, Long userId);

	@Query("""
			select gl
			  from GrowthLog gl
			  left join fetch gl.kpiLinks l
			  left join fetch l.kpiCard kc
			 where gl.id = :growthLogId
			   and gl.user.id = :userId
		""")
	Optional<GrowthLog> findDetailByIdAndUserId(
		@Param("growthLogId") Long growthLogId,
		@Param("userId") Long userId
	);
}
