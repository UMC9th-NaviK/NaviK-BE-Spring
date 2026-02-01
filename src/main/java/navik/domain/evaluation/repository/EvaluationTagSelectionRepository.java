package navik.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.evaluation.entity.EvaluationTagSelection;

public interface EvaluationTagSelectionRepository extends JpaRepository<EvaluationTagSelection, Long> {
}
