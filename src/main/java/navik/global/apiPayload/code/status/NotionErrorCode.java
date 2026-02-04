package navik.global.apiPayload.code.status;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotionErrorCode implements BaseCode {

	/**
	 * 사용자가 Notion 연동 권한 부여를 거부한 경우
	 */
	OAUTH_DENIED(HttpStatus.BAD_REQUEST, "NOTION400_1", "Notion 연동이 거부되었습니다."),

	/**
	 * OAuth 콜백에서 state 파라미터가 누락된 경우
	 */
	OAUTH_STATE_MISSING(HttpStatus.BAD_REQUEST, "NOTION400_2", "잘못된 요청입니다. (state 누락)"),

	/**
	 * state에서 userId 파싱 실패 (숫자 형식 아님)
	 */
	OAUTH_INVALID_USER_ID(HttpStatus.BAD_REQUEST, "NOTION400_3", "잘못된 userId 형식입니다."),

	/**
	 * Notion OAuth 토큰 교환 실패
	 */
	OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTION500_1", "Notion 연동 중 오류가 발생했습니다."),
	NOTION_OAUTH_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "NOTION_500_2", "Notion OAuth 토큰 교환 응답이 null입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
