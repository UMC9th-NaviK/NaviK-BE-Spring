package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Getter
@RequiredArgsConstructor
public enum MajorType {

	IT("IT"),
	DESIGN_MEDIA("디자인/미디어"),
	NATURAL_SCIENCE_BIO("자연/과학/바이오"),
	ENGINEERING("공학"),
	HUMANITIES_SOCIAL_EDUCATION("인문/사회/교육"),
	BUSINESS_ECONOMY_OFFICE("경영/경제/사무");

	private final String label;

	@JsonCreator
	public static MajorType deserialize(String majorType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(majorType))
			.findAny()
			.orElseThrow(() -> new GeneralExceptionHandler(RecruitmentErrorCode.MAJOR_TYPE_NOT_FOUND));
	}
}
