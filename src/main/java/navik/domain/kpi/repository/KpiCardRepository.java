package navik.domain.kpi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.projection.KpiCardGridItemView;

@Repository
public interface KpiCardRepository extends JpaRepository<KpiCard, Long> {

	@Query("""
		select c.id as id, c.name as name
		from KpiCard c
		where c.job.id = :jobId
		order by c.id asc
		""")
	List<KpiCardGridItemView> findGridByJobId(@Param("jobId") Long jobId);

	Optional<KpiCard> findById(Long id);

}
