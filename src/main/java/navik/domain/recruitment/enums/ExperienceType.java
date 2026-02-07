package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum ExperienceType {

	ENTRY("신입", 1),
	EXPERIENCED("경력", 2);

	private final String label;
	private final int order;

	@JsonCreator
	public static ExperienceType deserialize(String experienceType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(experienceType))
			.findAny()
			.orElseThrow(() -> new GeneralException(RecruitmentErrorCode.EXPERIENCE_TYPE_NOT_FOUND));
	}
}
