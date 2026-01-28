package navik.domain.evaluation.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class EvaluationSubmitDTO {

	@Getter
	@NoArgsConstructor
	public static class EvaluationSubmit {
		@NotNull
		private Long targetUserId;
		@NotNull
		@Min(1)
		@Max(5)
		private Float score;
		@Size(min = 5, max = 5, message = "이런 점이 뛰어나요 태그 5개를 선택해야 합니다.")
		private List<Long> strengthTagIds;
		@Size(min = 5, max = 5, message = "보완하면 좋아요 태그 5개를 선택해야 합니다.")
		private List<Long> weaknessTagIds;

		private String advice;
	}
}
