package navik.domain.evaluation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationMyDTO {
	private Double averageScore;
	private List<String> topStrengths; // 3개씩
	private List<String> topImprovement;
}