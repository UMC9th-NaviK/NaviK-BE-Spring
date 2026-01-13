package navik.domain.kpi.repository;

import navik.domain.kpi.repository.projection.KpiCardGridItemView;
import org.springframework.data.repository.query.Param;
import java.util.List;
import navik.domain.kpi.entity.KpiCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KpiCardRepository extends JpaRepository<KpiCard, Long> {

    @Query("""
            select c.id as id, c.name as name
            from KpiCard c
            where c.job.id = :jobId
            order by c.id asc
            """)
    List<KpiCardGridItemView> findGridByJobId(@Param("jobId") Long jobId);

}
