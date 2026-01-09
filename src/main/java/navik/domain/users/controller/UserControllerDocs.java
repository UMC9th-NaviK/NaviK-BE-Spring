package navik.domain.users.controller;

import navik.domain.users.dto.UserResponseDTO;
import navik.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.global.auth.annotation.AuthUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {
	@Operation(summary = "내 정보", description = "로그인한 사용자의 정보를 가져옵니다")
	ApiResponse<UserResponseDTO.UserInfoDTO> getMyInfo(@AuthUser Long userId);

	@Operation(summary = "사용자 조회", description = "특정 사용자의 정보를 가져옵니다")
	ApiResponse<UserResponseDTO.UserInfoDTO> getUser(@PathVariable Long userId);
}
