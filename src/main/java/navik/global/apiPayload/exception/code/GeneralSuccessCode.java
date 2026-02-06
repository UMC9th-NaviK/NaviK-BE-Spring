package navik.global.apiPayload.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/**
 * 일반적인 성공 코드를 정의하는 열거형입니다.
 * {@link BaseCode}를 구현하여 HTTP 상태, 코드, 메시지를 제공합니다.
 */
@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseCode {
	_OK(HttpStatus.OK, "COMMON200", "성공입니다."),
	_CREATED(HttpStatus.CREATED, "COMMON201", "요청 성공 및 리소스 생성됨"),
	_DELETED(HttpStatus.NO_CONTENT, "COMMON204", "삭제가 완료되었습니다."),
    _REDIRECT(HttpStatus.SEE_OTHER, "COMMON303", "요청이 성공적으로 처리되어 다른 리소스로 이동합니다.");

	/**
	 * HTTP 상태 코드
	 */
	private final HttpStatus httpStatus;

	/**
	 * 성공 코드
	 */
	private final String code;

	/**
	 * 성공 메시지
	 */
	private final String message;
}
