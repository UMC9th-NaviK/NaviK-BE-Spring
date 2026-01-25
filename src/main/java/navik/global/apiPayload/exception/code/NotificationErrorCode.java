package navik.global.apiPayload.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseCode {

	UNSUPPORTED_NOTIFICATION_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_01", "지원하지 않는 알림 타입입니다. ");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}