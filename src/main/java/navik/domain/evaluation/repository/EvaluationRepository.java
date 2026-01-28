package navik.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.evaluation.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
}
