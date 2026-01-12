package navik.domain.recruitment.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {
	
	DUPLICATE_POST_ID(HttpStatus.CONFLICT, "RECRUIT409_1", "이미 등록된 공고입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
