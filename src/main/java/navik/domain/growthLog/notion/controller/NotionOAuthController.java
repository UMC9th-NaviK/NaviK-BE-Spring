package navik.domain.growthLog.notion.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.notion.config.NotionOAuthProperties;
import navik.domain.growthLog.notion.dto.NotionOAuthResponse;
import navik.domain.growthLog.notion.service.NotionOAuthService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@Slf4j
@RestController
@RequestMapping("/api/notion/oauth")
@RequiredArgsConstructor
public class NotionOAuthController implements NotionOAuthControllerDocs {

	private final NotionOAuthService oAuthService;
	private final NotionOAuthProperties properties;

	/**
	 * STEP 1: Notion OAuth 인증 URL 반환
	 * 프론트엔드에서 이 URL로 사용자를 이동시킴
	 *
	 * @param userId 사용자 식별자
	 */
	@GetMapping("/authorize")
	public ApiResponse<NotionOAuthResponse.AuthorizeResponse> authorize(@AuthUser Long userId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK,
			new NotionOAuthResponse.AuthorizeResponse(oAuthService.buildAuthorizationUrl(userId)));
	}

	/**
	 * STEP 2: Notion에서 콜백 수신 및 토큰 교환
	 * Authorization Code를 Access Token으로 교환 후 저장
	 *
	 * @param code  Authorization Code (Notion에서 전달)
	 * @param state userId (인증 시작 시 전달한 값)
	 * @param error 에러 코드 (사용자가 거부한 경우)
	 */
	@GetMapping("/callback")
	public ApiResponse<Void> callback(@RequestParam(value = "code", required = false) String code,
		@RequestParam(value = "state", required = false) String state,
		@RequestParam(value = "error", required = false) String error) {

		String frontendUri = properties.oauth().frontendRedirectUri();

		if (error != null) {
			log.warn("Notion OAuth 거부됨: error={}, userId={}", error, state);
			return redirectToFrontend(frontendUri, "denied");
		}

		if (state == null || state.isBlank()) {
			log.error("Notion OAuth 콜백 오류: state 누락");
			return redirectToFrontend(frontendUri, "state_missing");
		}

		Long userId;
		try {
			userId = Long.parseLong(state.replace("user-", ""));
		} catch (NumberFormatException e) {
			log.error("잘못된 userId 형식(숫자 아님): {}", state);
			return redirectToFrontend(frontendUri, "invalid_state");
		}

		try {
			NotionOAuthResponse.TokenResponse tokenResponse = oAuthService.exchangeCodeForToken(code);
			oAuthService.saveToken(userId, tokenResponse);
			log.info("Notion 연동 완료: userId={}, workspace={}, workspaceId={}", userId, tokenResponse.workspaceName(),
				tokenResponse.workspaceId());
		} catch (Exception e) {
			log.error("Notion OAuth 토큰 교환 실패: userId={}", userId, e);
		}

		return redirectToFrontend(frontendUri, null);
	}

	private ApiResponse<Void> redirectToFrontend(String uri, String errorCode) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);

		if (errorCode == null) {
			builder.queryParam("success", true);
		} else {
			builder.queryParam("success", false);
			builder.queryParam("error", errorCode);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(builder.build().toUri());

		return ApiResponse.onSuccess(headers, GeneralSuccessCode._FOUND_REDIRECT, null);
	}

	// /**
	//  * 연동 상태 확인 (워크스페이스 목록 포함)
	//  */
	// @GetMapping("/status")
	// public ResponseEntity<NotionOAuthResponse.StatusResponse> status(@RequestParam("user_id") @AuthUser Long userId) {
	// 	boolean connected = oAuthService.isConnected(userId);
	// 	List<NotionOAuthResponse.WorkspaceInfo> workspaces = oAuthService.getConnectedWorkspaces(userId);
	// 	return ResponseEntity.ok(new NotionOAuthResponse.StatusResponse(String.valueOf(userId), connected, workspaces));
	// }
	//
	// /**
	//  * 연동 해제 (workspace_id 지정 시 해당 워크스페이스만, 미지정 시 전체 해제)
	//  */
	// @DeleteMapping("/disconnect")
	// public ResponseEntity<NotionOAuthResponse.DisconnectResponse> disconnect(
	// 		@RequestParam("user_id") @AuthUser Long userId,
	// 		@RequestParam(value = "workspace_id", required = false) String workspaceId) {
	// 	if (workspaceId != null && !workspaceId.isBlank()) {
	// 		oAuthService.disconnect(userId, workspaceId);
	// 		log.info("Notion 워크스페이스 연동 해제: userId={}, workspaceId={}", userId, workspaceId);
	// 		return ResponseEntity.ok(new NotionOAuthResponse.DisconnectResponse(
	// 				String.valueOf(userId), "Notion 워크스페이스 연동이 해제되었습니다.", workspaceId));
	// 	} else {
	// 		oAuthService.disconnectAll(userId);
	// 		log.info("Notion 전체 연동 해제: userId={}", userId);
	// 		return ResponseEntity.ok(new NotionOAuthResponse.DisconnectResponse(
	// 				String.valueOf(userId), "Notion 전체 연동이 해제되었습니다.", null));
	// 	}
	// }
}
