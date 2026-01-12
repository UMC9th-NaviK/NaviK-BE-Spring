package navik.domain.job.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum JobErrorCode implements BaseCode {

	NOT_FOUND_JOB(HttpStatus.NOT_FOUND, "JOB404_1", "존재하지 않는 직무입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
