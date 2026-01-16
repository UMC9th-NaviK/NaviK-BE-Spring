package navik.domain.users.service;

import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;
import navik.domain.users.entity.UserDepartment;
import navik.domain.users.repository.UserDepartmentRepository;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.deprtment.UserDepartmentQueryService;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserRepository userRepository;
	private final UserDepartmentRepository userDepartmentRepository;
	private final ConversionService conversionService;
	private final UserDepartmentQueryService userDepartmentQueryService;

	public UserResponseDTO.UserInfoDTO getUserInfo(Long userId) {
		return conversionService.convert(getUser(userId), UserResponseDTO.UserInfoDTO.class);
	}

	public UserResponseDTO.UserInfoDTO getMyInfo(Long userId) {
		return conversionService.convert(getUser(userId), UserResponseDTO.UserInfoDTO.class);
	}

	public UserResponseDTO.NicknameCheckDto isNicknameDuplicated(String nickname) {
		return new UserResponseDTO.NicknameCheckDto(nickname, userRepository.existsByNickname(nickname));
	}

	public User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
	}

	public UserResponseDTO.ProfileDTO getProfile(Long userId) {
		return conversionService.convert(getUser(userId), UserResponseDTO.ProfileDTO.class);
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
