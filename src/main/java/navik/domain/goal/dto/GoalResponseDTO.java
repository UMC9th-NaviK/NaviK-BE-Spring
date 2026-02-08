package navik.domain.goal.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.goal.entity.GoalStatus;

public class GoalResponseDTO {

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class PreviewDTO {
		@NotNull
		private Long goalId;
		@NotNull
		private String title;
		@NotNull
		private GoalStatus status;
	}

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class InfoDTO {
		@NotNull
		private Long goalId;
		@NotNull
		private String title;
		@NotNull
		private String content;
		@NotNull
		private LocalDate endDate;
		@NotNull
		private GoalStatus status;
	}

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class InProgressDTO {
		@NotNull
		private List<InfoDTO> inProgressGoals;
		@NotNull
		private Long totalCount;
	}
}
