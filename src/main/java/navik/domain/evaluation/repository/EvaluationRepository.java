package navik.domain.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.evaluation.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
	boolean existsByStudyIdAndEvaluatorIdAndEvaluateeId(Long studyId, Long evaluatorId, Long evaluateeId);

	// 누적 평가 : 내가 받은 모든 평가 조회 , 메인화면
	// evaluatee.id를 기준으로 내가 받은 평가 모두 조회
	// submitEvaluation에서 평가 내용을 evaluatee에 저장했기 때문에
	@Query(
		"""
			SELECT e 
			FROM Evaluation e 
			WHERE e.evaluatee.id = :userId
			"""
	)
	List<Evaluation> findAllByEvaluateeId(@Param("userId") Long userId);

	// 상세 조회 : 특정 스터디에서 내가 받은 평가만 조회
	@Query(
		"""
			SELECT e 
			FROM Evaluation e
			WHERE e.evaluatee.id = :userId
			AND e.study.id = :studyId		
			"""
	)
	List<Evaluation> findAllByEvaluateeIdAndStudyId(@Param("userId") Long userId, @Param("studyId") Long studyId);
}
