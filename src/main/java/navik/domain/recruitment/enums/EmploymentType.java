package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Getter
@RequiredArgsConstructor
public enum EmploymentType {

	FULL_TIME("정규직"),
	CONTRACT("계약직"),
	INTERN("인턴"),
	FREELANCER("프리랜서");

	private final String label;

	@JsonCreator
	public static EmploymentType deserialize(String employmentType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(employmentType))
			.findAny()
			.orElseThrow(() -> new GeneralExceptionHandler(RecruitmentErrorCode.EMPLOYMENT_TYPE_NOT_FOUND));
	}
}
