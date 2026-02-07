package navik.domain.study.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum StudyRole {

	STUDY_LEADER("스터디장"),
	STUDY_MEMBER("스터디원");

	private final String label;

	@JsonCreator
	public static StudyRole deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralException(StudyErrorCode.INVALID_STUDY_ROLE));
	}
}
