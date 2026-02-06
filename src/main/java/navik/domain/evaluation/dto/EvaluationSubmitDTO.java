package navik.domain.evaluation.dto;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluationSubmitDTO(
	@NotNull
	Long targetUserId,

	@NotNull
	@DecimalMin(value = "1.0")
	@DecimalMax(value = "5.0")
	Double score,

	@Size(min = 5, max = 5, message = "이런 점이 뛰어나요 태그 5개를 선택해야 합니다.")
	List<Long> strengthTagIds,

	@Size(min = 5, max = 5, message = "보완하면 좋아요 태그 5개를 선택해야 합니다.")
	List<Long> weaknessTagIds,

	String advice
) {
}

