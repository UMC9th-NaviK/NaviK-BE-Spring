package navik.domain.recruitment.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.exception.code.BaseCode;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {

	AREA_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_01", "존재하지 않는 지역 유형입니다."),
	COMPANY_SIZE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_02", "존재하지 않는 회사 규모입니다."),
	EMPLOYMENT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_03", "존재하지 않는 고용 형태입니다."),
	EXPERIENCE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_04", "존재하지 않는 경력 유형입니다."),
	INDUSTRY_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_05", "존재하지 않는 업종 유형입니다."),
	JOB_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_06", "존재하지 않는 직무 유형입니다."),
	MAJOR_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_404_07", "존재하지 않는 전공 유형입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
