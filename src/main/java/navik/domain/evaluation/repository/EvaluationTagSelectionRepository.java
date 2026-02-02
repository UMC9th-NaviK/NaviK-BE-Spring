package navik.domain.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.evaluation.entity.Evaluation;
import navik.domain.evaluation.entity.EvaluationTagSelection;

public interface EvaluationTagSelectionRepository extends JpaRepository<EvaluationTagSelection, Long> {
	// 여러 평가들에 속한 모든 태그 매핑 정보와 태그 엔티티를 한 번에 조회 (Fetch Join)
	@Query(
		"""
			SELECT ets 
			FROM EvaluationTagSelection ets 
			JOIN FETCH ets.tag
			WHERE ets.evaluation 
			IN :evaluations
			""")
	List<EvaluationTagSelection> findAllByEvaluationIn(@Param("evaluations") List<Evaluation> evaluations);
}
