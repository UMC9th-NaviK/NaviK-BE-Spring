package navik.domain.recruitment.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {

	AREA_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_01", "존재하지 않는 지역 유형입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
