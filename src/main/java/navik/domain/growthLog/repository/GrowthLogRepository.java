package navik.domain.growthLog.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthType;

@Repository
public interface GrowthLogRepository extends JpaRepository<GrowthLog, Long> {

	@Modifying
	@Query("""
			update GrowthLog g
			   set g.kpiCard.id = :kpiCardId,
			       g.score = :score
			 where g.id = :growthLogId
			   and g.type = :type
			   and g.kpiCard is null
		""")
	int applyUserInputResultOnce(
		@Param("growthLogId") Long growthLogId,
		@Param("kpiCardId") Long kpiCardId,
		@Param("score") Integer score,
		@Param("type") GrowthType type
	);

	@Query("""
		    select gl
		      from GrowthLog gl
		     where gl.user.id = :userId
		       and gl.createdAt >= :start
		       and gl.createdAt < :end
		     order by gl.createdAt desc
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
		     order by gl.createdAt desc
		""")
	Page<GrowthLog> findMonthlyByType(
		@Param("userId") Long userId,
		@Param("type") GrowthType type,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		Pageable pageable
	);

}
