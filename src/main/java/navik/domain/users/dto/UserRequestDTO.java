package navik.domain.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequestDTO {

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class BasicInfoDto {
		@NotBlank
		private String name;
		@NotBlank
		private String nickname;
		@NotNull
		private Long jobId;
		@NotNull
		private boolean isEntryLevel;
	}
}
