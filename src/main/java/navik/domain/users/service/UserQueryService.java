package navik.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.converter.UserConverter;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserRepository userRepository;

	public UserResponseDTO.UserInfoDTO getUserInfo(Long userId) {
		return UserConverter.toUserInfoDTO(getUser(userId));
	}

	public UserResponseDTO.UserInfoDTO getMyInfo(Long userId) {
		return UserConverter.toUserInfoDTO(getUser(userId));
	}

	public UserResponseDTO.NicknameCheckDto isNicknameDuplicated(String nickname) {
		return UserResponseDTO.NicknameCheckDto.builder()
			.nickname(nickname)
			.isDuplicated(userRepository.existsByNickname(nickname))
			.build();
	}

	public User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
	}

	public UserResponseDTO.ProfileDTO getProfile(Long userId) {
		return UserConverter.toProfileDTO(getUser(userId));
	}
}
