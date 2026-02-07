package navik.global.apiPayload.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseCode {

	UNSUPPORTED_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTIFICATION_400_01", "지원하지 않는 알림 타입입니다."),

	NOTIFICATION_NOT_OWNER(HttpStatus.FORBIDDEN, "NOTIFICATION_403_01", "알림 소유자가 아닙니다."),

	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_404_01", "알림을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}