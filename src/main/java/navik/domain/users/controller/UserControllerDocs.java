package navik.domain.users.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {

	@Operation(summary = "사용자 조회", description = "특정 사용자의 기본 정보(이름, 이메일, 역할, 소셜 타입)를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "사용자 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "id": 1,
			    "name": "홍길동",
			    "email": "hong@gmail.com",
			    "role": "USER",
			    "socialType": "google"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 사용자", summary = "해당 userId에 대한 사용자가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "USER_404",
			  "message": "존재하지 않는 회원입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<UserResponseDTO.UserInfoDTO> getUser(
		@Parameter(description = "조회할 사용자 ID", example = "1", required = true) @PathVariable Long userId);

	@Operation(summary = "사용자 초기 정보 등록 (온보딩)", description = """
		사용자 상태가 `PENDING`인 경우, 이름/닉네임/직무/신입 여부를 입력하여 가입을 완료합니다.
		- 성공 시 사용자 상태가 `ACTIVE`로 변경됩니다.
		- 온보딩 완료 후 `/v1/auth/refresh`를 호출하여 ACTIVE 상태의 새 액세스 토큰을 발급받아야 합니다.
		""")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "온보딩 등록 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "id": 1,
			    "name": "홍길동",
			    "nickname": "길동이",
			    "jobId": 3,
			    "isEntryLevel": true
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "필수 값 누락", summary = "name, nickname, jobId, isEntryLevel 중 누락된 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_400_01",
			  "message": "입력값이 올바르지 않습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 사용자", summary = "토큰의 userId에 해당하는 사용자가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "USER_404",
			  "message": "존재하지 않는 회원입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "닉네임 중복", summary = "이미 사용 중인 닉네임으로 등록 시도한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "DB_409",
			  "message": "데이터 무결성 제약 조건을 위반했습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<UserResponseDTO.BasicInfoDto> register(@Parameter(hidden = true) @AuthUser Long userId,
		@RequestBody @Valid UserRequestDTO.BasicInfoDTO req);

	@Operation(summary = "닉네임 중복 확인", description = "입력받은 닉네임이 DB에 이미 존재하는지 확인합니다. 사용 가능하면 `false`(중복 아님), 이미 존재하면 `true`(중복)를 반환합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공", content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "사용 가능한 닉네임", summary = "중복되지 않는 닉네임인 경우", value = """
				{
				  "isSuccess": true,
				  "code": "COMMON200",
				  "message": "성공입니다.",
				  "result": {
				    "nickname": "길동이",
				    "isDuplicated": false
				  },
				  "timestamp": "2026-01-20T14:30:00"
				}
				"""), @ExampleObject(name = "중복된 닉네임", summary = "이미 사용 중인 닉네임인 경우", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "nickname": "길동이",
			    "isDuplicated": true
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")})),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<UserResponseDTO.NicknameCheckDto> checkNicknameDuplication(
		@Parameter(description = "중복 확인할 닉네임", example = "길동이", required = true) @RequestParam String nickname);

	@Operation(summary = "마이 페이지", description = "로그인한 사용자의 전체 정보를 조회합니다. 프로필 이미지, 이름, 닉네임, 직무, 신입/경력 여부, 학력, 학과 목록을 포함합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "마이 페이지 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "profileImageUrl": "https://example.com/images/profile.jpg",
			    "name": "홍길동",
			    "nickname": "길동이",
			    "job": "백엔드 개발자",
			    "isEntryLevel": true,
			    "educationLevel": "BACHELOR",
			    "departmentList": ["컴퓨터공학과", "소프트웨어학과"]
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_403_02",
			  "message": "온보딩이 완료되지 않았습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 사용자", value = """
			{
			  "isSuccess": false,
			  "code": "USER_404",
			  "message": "존재하지 않는 회원입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<UserResponseDTO.MyPageDTO> getMyInfo(@Parameter(hidden = true) @AuthUser Long userId);

	@Operation(summary = "프로필 조회", description = "로그인한 사용자의 프로필 요약 정보(프로필 이미지, 닉네임, 직무, 신입/경력 여부)를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "프로필 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "profileImageUrl": "https://example.com/images/profile.jpg",
			    "nickname": "길동이",
			    "job": "백엔드 개발자",
			    "isEntryLevel": true
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_403_02",
			  "message": "온보딩이 완료되지 않았습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 사용자", value = """
			{
			  "isSuccess": false,
			  "code": "USER_404",
			  "message": "존재하지 않는 회원입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<UserResponseDTO.ProfileDTO> getProfile(@Parameter(hidden = true) @AuthUser Long userId);
}
