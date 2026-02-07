package navik.domain.users.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.service.UserCommandService;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController implements UserControllerDocs {

	private final UserQueryService userQueryService;
	private final UserCommandService userCommandService;

	@GetMapping("/{userId}")
	public ApiResponse<UserResponseDTO.UserInfoDTO> getUser(@PathVariable Long userId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userQueryService.getUserInfo(userId));
	}

	@PostMapping("/me/basic-info")
	public ApiResponse<UserResponseDTO.BasicInfoDto> register(@AuthUser Long userId,
		@RequestBody @Valid UserRequestDTO.BasicInfoDto req) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userCommandService.updateBasicInfo(userId, req));
	}

	@GetMapping("/check-nickname")
	public ApiResponse<UserResponseDTO.NicknameCheckDto> checkNicknameDuplication(@RequestParam String nickname) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userQueryService.isNicknameDuplicated(nickname));
	}

	@GetMapping("/profile")
	public ApiResponse<UserResponseDTO.ProfileDTO> getProfile(@AuthUser Long userId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userQueryService.getProfile(userId));
	}

	@GetMapping("/my-page")
	public ApiResponse<UserResponseDTO.MyPageDTO> getMyInfo(@AuthUser Long userId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, userQueryService.getMyPage(userId));
	}

	@PatchMapping("/my-page")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateMyInfo(@AuthUser Long userId, @Valid @RequestBody UserRequestDTO.MyInfoDto req) {
		userCommandService.updateMyInfo(userId, req);
	}

	@PutMapping("/profile-image")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProfileImage(@AuthUser Long userId, @RequestBody String imageUrl) {
		userCommandService.updateProfileImage(userId, imageUrl);
	}

}
