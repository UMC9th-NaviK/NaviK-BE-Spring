package navik.domain.users.dto;

import navik.domain.users.enums.Role;

public class UserResponseDTO {

	public record UserInfoDTO(
		Long id,
		String name,
		String email,
		Role role,
		String socialType
	) {}

	public record BasicInfoDto(
		Long id,
		String name,
		String nickname,
		Long jobId,
		Boolean isEntryLevel
	) {}

	public record NicknameCheckDto(
		String nickname,
		boolean isDuplicated
	) {}

	public record ProfileDTO(
		String profileImageUrl,
		String nickname,
		String job,
		boolean isEntryLevel
	) {}

	public record MyPageDTO(
		String profileImageUrl,
		String name,
		String nickname,
		String job,
		boolean isEntryLevel
	) {}
}
