package navik.domain.users.dto;

import java.util.List;

import navik.domain.users.enums.EducationLevel;
import navik.domain.users.enums.Role;

public class UserResponseDTO {

	public record UserInfoDTO(
		Long id,
		String name,
		String email,
		Role role,
		String socialType
	) {
	}

	public record BasicInfoDTO(
		Long id,
		String name,
		String nickname,
		Long jobId,
		Boolean isEntryLevel
	) {
	}

	public record NicknameCheckDTO(
		String nickname,
		boolean isDuplicated
	) {
	}

	public record ProfileDTO(
		Long id,
		String profileImageUrl,
		String nickname,
		String job,
		Boolean isEntryLevel
	) {
	}

	public record MyPageDTO(
		Long id,
		String profileImageUrl,
		String name,
		String nickname,
		String job,
		Boolean isEntryLevel,
		EducationLevel educationLevel,
		List<Long> departmentList
	) {
	}

}
