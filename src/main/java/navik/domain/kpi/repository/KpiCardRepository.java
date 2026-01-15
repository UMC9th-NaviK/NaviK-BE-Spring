package navik.domain.kpi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import navik.domain.kpi.entity.KpiCard;

@Repository
public interface KpiCardRepository extends CrudRepository<KpiCard, Long> {
}
