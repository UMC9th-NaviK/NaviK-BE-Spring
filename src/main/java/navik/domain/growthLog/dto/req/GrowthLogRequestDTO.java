package navik.domain.growthLog.dto.req;

import jakarta.validation.constraints.NotBlank;

public class GrowthLogRequestDTO {

	public record CreateUserInput(
		@NotBlank String title,
		@NotBlank String content
	) {
	}

}