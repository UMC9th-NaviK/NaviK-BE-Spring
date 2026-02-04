package navik.domain.growthLog.notion.controller;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.growthLog.notion.dto.NotionOAuthResponse;

@Tag(name = "Notion OAuth", description = "Notion 연동 OAuth 관련 API")
public interface NotionOAuthControllerDocs {

	@Operation(summary = "Notion OAuth 인증 시작", description = "사용자를 Notion 인증 페이지로 리다이렉트합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "302", description = "Notion 인증 페이지로 리다이렉트")
	})
	ResponseEntity<Void> authorize(
		@Parameter(description = "사용자 ID", required = true, example = "1") Long userId);

	@Operation(summary = "Notion OAuth 콜백", description = "Notion에서 Authorization Code를 수신하고 Access Token으로 교환 후 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Notion 연동 성공"),
		@ApiResponse(responseCode = "400", description = "사용자가 연동을 거부하거나 잘못된 요청"),
		@ApiResponse(responseCode = "500", description = "토큰 교환 중 오류 발생")
	})
	ResponseEntity<NotionOAuthResponse.CallbackResponse> callback(
		@Parameter(description = "Authorization Code (Notion에서 전달)", required = false) String code,
		@Parameter(description = "사용자 ID (인증 시작 시 전달한 state 값)", required = false) String state,
		@Parameter(description = "에러 코드 (사용자가 거부한 경우)", required = false) String error);

	// @Operation(summary = "Notion 연동 상태 확인", description = "사용자의 Notion 연동 상태와 연결된 워크스페이스 목록을 조회합니다.")
	// @ApiResponses({
	// 	@ApiResponse(responseCode = "200", description = "연동 상태 조회 성공")
	// })
	// ResponseEntity<NotionOAuthResponse.StatusResponse> status(
	// 	@Parameter(description = "사용자 ID", required = true, example = "1") Long userId);
	//
	// @Operation(summary = "Notion 연동 해제", description = "특정 워크스페이스 또는 전체 Notion 연동을 해제합니다.")
	// @ApiResponses({
	// 	@ApiResponse(responseCode = "200", description = "연동 해제 성공")
	// })
	// ResponseEntity<NotionOAuthResponse.DisconnectResponse> disconnect(
	// 	@Parameter(description = "사용자 ID", required = true, example = "1") Long userId,
	// 	@Parameter(description = "워크스페이스 ID (미지정 시 전체 해제)", required = false) String workspaceId);
}
