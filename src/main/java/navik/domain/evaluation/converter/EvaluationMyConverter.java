package navik.domain.evaluation.converter;

import java.util.List;

import navik.domain.evaluation.dto.EvaluationMyDTO;

public class EvaluationMyConverter {

	public static EvaluationMyDTO toEvaluationMyDTO(
		Double averageScore,
		List<String> strengths,
		List<String> improvement
	) {
		return EvaluationMyDTO.builder()
			.averageScore(averageScore)
			.topStrengths(strengths)
			.topImprovement(improvement)
			.build();
	}
}