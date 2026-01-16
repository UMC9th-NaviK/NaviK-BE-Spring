package navik.domain.users.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;

public class UserConverter {

	@Component
	public static class UserToUserInfoDTOConverter implements Converter<User, UserResponseDTO.UserInfoDTO> {
		@Override
		public UserResponseDTO.UserInfoDTO convert(User user) {
			return new UserResponseDTO.UserInfoDTO(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getSocialType()
			);
		}
	}

	@Component
	public static class UserToBasicInfoDtoConverter implements Converter<User, UserResponseDTO.BasicInfoDto> {
		@Override
		public UserResponseDTO.BasicInfoDto convert(User user) {
			return new UserResponseDTO.BasicInfoDto(
				user.getId(),
				user.getName(),
				user.getNickname(),
				user.getJob().getId(),
				user.getIsEntryLevel()
			);
		}
	}

	@Component
	public static class UserToProfileDTOConverter implements Converter<User, UserResponseDTO.ProfileDTO> {
		@Override
		public UserResponseDTO.ProfileDTO convert(User user) {
			return new UserResponseDTO.ProfileDTO(
				user.getProfileImageUrl(),
				user.getNickname(),
				user.getJob().getName(),
				user.getIsEntryLevel()
			);
		}
	}

	@Component
	public static class UserToMyPageDTOConverter implements Converter<User, UserResponseDTO.MyPageDTO> {
		@Override
		public UserResponseDTO.MyPageDTO convert(User user) {
			return new UserResponseDTO.MyPageDTO(
				user.getProfileImageUrl(),
				user.getName(),
				user.getNickname(),
				user.getJob().getName(),
				user.getIsEntryLevel()
			);
		}
	}
}
