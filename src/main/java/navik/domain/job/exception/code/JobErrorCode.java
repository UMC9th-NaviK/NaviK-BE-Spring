package navik.domain.job.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum JobErrorCode implements BaseCode {

	JOB_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"JOB_404_01",
		"존재하지 않는 KPI 카드입니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
