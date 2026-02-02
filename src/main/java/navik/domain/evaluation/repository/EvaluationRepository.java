package navik.domain.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import navik.domain.evaluation.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
	boolean existsByStudyIdAndEvaluatorIdAndEvaluateeId(Long studyId, Long evaluatorId, Long evaluateeId);

	// evaluatee.id를 기준으로 내가 받은 평가 모두 조회
	// submitEvaluation에서 평가 내용을 evaluatee에 저장했기 때문에
	@Query(
		"""
			SELECT e 
			FROM Evaluation e 
			WHERE e.evaluatee.id = :userId
			"""
	)
	List<Evaluation> findAllByEvaluateeId(Long userId);
}
