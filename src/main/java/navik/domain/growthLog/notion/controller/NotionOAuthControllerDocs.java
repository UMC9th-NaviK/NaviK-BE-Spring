package navik.domain.growthLog.notion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.growthLog.notion.dto.NotionOAuthResponse;
import navik.global.apiPayload.ApiResponse.Body;

@Tag(name = "Notion OAuth", description = "Notion 연동 OAuth 관련 API")
public interface NotionOAuthControllerDocs {

	@Operation(summary = "Notion OAuth 인증 URL 조회", description = "Notion OAuth 인증 URL을 반환합니다. 프론트엔드에서 이 URL로 사용자를 이동시킵니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "인증 URL 반환 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Body.class), examples = @ExampleObject(name = "인증 URL 반환 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "authorizationUrl": "https://api.notion.com/v1/oauth/authorize?client_id=...&redirect_uri=...&response_type=code&owner=user&state=user-1"
			  },
			  "timestamp": "2025-02-05T12:00:00"
			}
			"""))),
		@ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Body.class), examples = @ExampleObject(name = "인증 토큰 없음", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH4001",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2025-02-05T12:00:00"
			}
			""")))})
	navik.global.apiPayload.ApiResponse<NotionOAuthResponse.AuthorizeResponse> authorize(
		@Parameter(hidden = true) Long userId);

	@Operation(summary = "(프론트 사용 X) Notion OAuth 콜백", description = "Notion에서 Authorization Code를 수신하고 Access Token으로 교환 후 저장합니다. 처리 완료 후 프론트엔드로 302 리다이렉트합니다. (Notion 리다이렉트용 - 인증 불필요, 프론트 사용x)")
	navik.global.apiPayload.ApiResponse<Void> callback(
		@Parameter(description = "Authorization Code (Notion에서 전달)") String code,
		@Parameter(description = "사용자 ID (인증 시작 시 전달한 state 값)") String state,
		@Parameter(description = "에러 코드 (사용자가 거부한 경우)") String error);
}
