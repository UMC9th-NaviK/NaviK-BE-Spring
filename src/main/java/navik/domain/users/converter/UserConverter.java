package navik.domain.users.converter;

import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;

public class UserConverter {

	public static UserResponseDTO.UserInfoDTO toUserInfoDTO(User user) {
		return UserResponseDTO.UserInfoDTO.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole())
			.socialType(user.getSocialType())
			.build();
	}

	public static UserResponseDTO.BasicInfoDto toBasicInfoDto(User user) {
		return UserResponseDTO.BasicInfoDto.builder()
			.id(user.getId())
			.name(user.getName())
			.nickname(user.getNickname())
			.jobId(user.getJob().getId())
			.isEntryLevel(user.getIsEntryLevel())
			.build();
	}

	public static UserResponseDTO.ProfileDTO toProfileDTO(User user) {
		return UserResponseDTO.ProfileDTO.builder()
			.profileImageUrl(user.getProfileImageUrl())
			.nickname(user.getNickname())
			.job(user.getJob().getName())
			.isEntryLevel(user.getIsEntryLevel())
			.build();
	}

	public static UserResponseDTO.MyPageDTO toMyPageDTO(User user) {
		return UserResponseDTO.MyPageDTO.builder()
			.profileImageUrl(user.getProfileImageUrl())
			.name(user.getName())
			.nickname(user.getNickname())
			.job(user.getJob().getName())
			.isEntryLevel(user.getIsEntryLevel())
			.build();
	}
}
