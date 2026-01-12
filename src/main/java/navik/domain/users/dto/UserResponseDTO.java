package navik.domain.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.users.enums.Role;

public class UserResponseDTO {

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class UserInfoDTO {
		private Long id;
		private String name;
		private String email;
		private Role role;
		private String socialType;
	}

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class BasicInfoDto {
		@NotNull
		private Long id;
		@NotBlank
		private String name;
		@NotBlank
		private String nickname;
		@NotNull
		private Long jobId;
		@NotNull
		private Boolean isEntryLevel;
	}

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class NicknameCheckDto {
		@NotNull
		private String nickname;
		@NotNull
		private boolean isDuplicated;
	}
}
