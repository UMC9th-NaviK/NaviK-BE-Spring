package navik.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.evaluation.entity.EvaluationTag;

public interface EvaluationTagRepository extends JpaRepository<EvaluationTag, Long> {
}
