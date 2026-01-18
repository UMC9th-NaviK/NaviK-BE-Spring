package navik.domain.growthLog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.growthLog.entity.GrowthLogKpiLink;

public interface GrowthLogKpiLinkRepository extends JpaRepository<GrowthLogKpiLink, Long> {

	@Query("""
			select l
			  from GrowthLogKpiLink l
			  join fetch l.growthLog
			  join fetch l.kpiCard
			 where l.growthLog.id in :ids
		""")
	List<GrowthLogKpiLink> findByGrowthLogIdIn(@Param("ids") List<Long> growthLogIds);
}
