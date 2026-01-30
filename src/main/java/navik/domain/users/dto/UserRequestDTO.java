package navik.domain.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRequestDTO {

	public record BasicInfoDto(
		@NotBlank String name,
		@NotBlank String nickname,
		@NotNull Long jobId,
		@NotNull Boolean isEntryLevel
	) {}
}
