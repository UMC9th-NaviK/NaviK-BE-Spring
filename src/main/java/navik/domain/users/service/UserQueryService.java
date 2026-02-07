package navik.domain.users.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.departments.UserDepartmentQueryService;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserRepository userRepository;
	private final UserDepartmentQueryService userDepartmentQueryService;

	public UserResponseDTO.UserInfoDTO getUserInfo(Long userId) {
		return userRepository.findUserInfoById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
	}

	public UserResponseDTO.NicknameCheckDto isNicknameDuplicated(String nickname) {
		return new UserResponseDTO.NicknameCheckDto(nickname, userRepository.existsByNickname(nickname));
	}

	public User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
	}

	public UserResponseDTO.ProfileDTO getProfile(Long userId) {
		return userRepository.findProfileById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
	}

	public UserResponseDTO.MyPageDTO getMyPage(Long userId) {
		User user = getUser(userId);
		List<String> departmentList = userDepartmentQueryService.getUserDepartments(userId);

		return new UserResponseDTO.MyPageDTO(
			user.getProfileImageUrl(),
			user.getName(),
			user.getNickname(),
			user.getJob().getName(),
			user.getIsEntryLevel(),
			user.getEducationLevel(),
			departmentList
		);
	}
}
