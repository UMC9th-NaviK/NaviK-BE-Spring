package navik.domain.users.controller;

import jakarta.validation.Valid;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {
	@Operation(summary = "내 정보", description = "로그인한 사용자의 정보를 가져옵니다")
	ApiResponse<UserResponseDTO.UserInfoDTO> getMyInfo(@AuthUser Long userId);

	@Operation(summary = "사용자 조회", description = "특정 사용자의 정보를 가져옵니다")
	ApiResponse<UserResponseDTO.UserInfoDTO> getUser(@PathVariable Long userId);

	@Operation(summary = "사용자 초기 정보 등록", description = "사용자 상태가 `PENDING`인 경우, 이름, 닉네임, 직무 등 필수 정보를 입력하여 가입을 완료합니다.")
	ApiResponse<UserResponseDTO.BasicInfoDto> register(@AuthUser Long userId, @RequestBody @Valid UserRequestDTO.BasicInfoDto req);

	@Operation(summary = "닉네임 중복 확인", description = "입력받은 닉네임이 DB에 이미 존재하는지 확인합니다. 사용 가능하면 false(중복 아님), 이미 존재하면 true(중복)를 반환합니다.")
	ApiResponse<UserResponseDTO.NicknameCheckDto> checkNicknameDuplication(String nickname);


}
