package navik.domain.users.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.exception.code.BaseCode;

@Getter
@AllArgsConstructor
public enum JobErrorCode implements BaseCode {

	JOB_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"JOB_404_01",
		"존재하지 않는 직무입니다."
	),

	JOB_NOT_ASSIGNED(
		HttpStatus.BAD_REQUEST,
		"JOB_400_01",
		"사용자에게 직무가 설정되어 있지 않습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}