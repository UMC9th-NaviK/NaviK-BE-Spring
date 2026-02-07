package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum CompanySize {

	LARGE("대기업"),
	MID_LARGE("중견기업"),
	SMALL("중소기업"),
	PUBLIC("공기업"),
	FOREIGN("외국계기업");

	private final String label;

	@JsonCreator
	public static CompanySize deserialize(String companySize) {
		return Arrays.stream(values())
			.filter(size -> size.name().equalsIgnoreCase(companySize))
			.findAny()
			.orElseThrow(() -> new GeneralException(RecruitmentErrorCode.COMPANY_SIZE_NOT_FOUND));
	}
}
