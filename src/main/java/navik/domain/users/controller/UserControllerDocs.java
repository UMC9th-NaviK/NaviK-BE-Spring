package navik.domain.users.controller;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {

	@Operation(summary = "사용자 조회", description = "특정 사용자의 정보를 가져옵니다")
	ApiResponse<UserResponseDTO.UserInfoDTO> getUser(@PathVariable Long userId);

	@Operation(summary = "사용자 초기 정보 등록", description = "사용자 상태가 `PENDING`인 경우, 이름, 닉네임, 직무 등 필수 정보를 입력하여 가입을 완료합니다.")
	ApiResponse<UserResponseDTO.BasicInfoDto> register(@AuthUser Long userId,
		@RequestBody UserRequestDTO.BasicInfoDto req);

	@Operation(summary = "닉네임 중복 확인", description = "입력받은 닉네임이 DB에 이미 존재하는지 확인합니다. 사용 가능하면 false(중복 아님), 이미 존재하면 true(중복)를 반환합니다.")
	ApiResponse<UserResponseDTO.NicknameCheckDto> checkNicknameDuplication(String nickname);

	@Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 가져옵니다(마이 페이지)")
	ApiResponse<UserResponseDTO.MyPageDTO> getMyInfo(@AuthUser Long userId);

	@Operation(summary = "내 정보 수정", description = "요청 본문에 포함된 필드만 수정되며, null인 필드는 기존 값을 유지합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공 (응답 본문 없음)")})
	void updateMyInfo(@AuthUser Long userId, @RequestBody UserRequestDTO.MyInfoDto req);

	@Operation(summary = "내 프로필 이미지 수정", description = "사용자의 프로필 이미지를 수정합니다")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공 (응답 본문 없음)")})
	void updateProfileImage(@AuthUser Long userId, @RequestBody String imageUrl);

	@Operation(summary = "프로필", description = "로그인한 사용자의 프로필(요약)을 가져옵니다")
	ApiResponse<UserResponseDTO.ProfileDTO> getProfile(@AuthUser Long userId);

}
