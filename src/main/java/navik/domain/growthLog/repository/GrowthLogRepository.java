package navik.domain.growthLog.repository;

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

}
