package navik.domain.term.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.term.dto.TermRequestDTO;
import navik.domain.term.dto.TermResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Term", description = "약관 관련 API")
public interface TermControllerDocs {

	@Operation(summary = "약관 전체 조회", description = "약관들을 전체 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "약관 전체 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": [
			    {
			      "id": 1,
			      "title": "서비스 이용약관",
			      "content": "서비스 이용약관 내용입니다.",
			      "updatedAt": "2026-01-20T14:30:00"
			    },
			    {
			      "id": 2,
			      "title": "개인정보 수집 및 이용 동의",
			      "content": "개인정보 수집 및 이용에 대한 내용입니다.",
			      "updatedAt": "2026-01-20T14:30:00"
			    }
			  ],
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<List<TermResponseDTO.TermInfo>> getTerms();

	@Operation(summary = "약관 상세 조회", description = "특정 약관의 상세 내용을 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "약관 상세 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "id": 1,
			    "title": "서비스 이용약관",
			    "content": "서비스 이용약관 내용입니다.",
			    "updatedAt": "2026-01-20T14:30:00"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약관 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 약관", summary = "해당 termId에 대한 약관이 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_404",
			  "message": "해당 리소스를 찾을 수 없습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<TermResponseDTO.TermInfo> getTerm(
		@Parameter(description = "조회할 약관 ID", example = "1", required = true) @PathVariable Long termId);

	@Operation(summary = "약관 동의", description = "선택한 약관들에 대해 일괄 동의합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "동의 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "약관 동의 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON201",
			  "message": "요청 성공 및 리소스 생성됨",
			  "result": {
			    "userId": 1,
			    "agreedTermIds": [1, 2, 3]
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
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 동의", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "이미 동의한 약관", summary = "이미 동의한 약관에 대해 중복 동의를 시도한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "DB_409",
			  "message": "데이터 무결성 제약 조건을 위반했습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<TermResponseDTO.AgreementResultDTO> agreeTerms(
		@Parameter(hidden = true) @AuthUser Long userId,
		@RequestBody @Valid TermRequestDTO.AgreeDTO req);
}
