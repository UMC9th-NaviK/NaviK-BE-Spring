package navik.domain.goal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GoalRequestDTO {

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class CreateDTO{
		@NotNull
		private String content;
		@NotNull
		private LocalDate endDate;
	}
}
