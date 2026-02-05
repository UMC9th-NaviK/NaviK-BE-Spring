package navik.domain.study.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Getter
@RequiredArgsConstructor
public enum StudySynergy {

	SAME_JOB("비슷한 직무끼리 모이기"),
	DIVERSE_JOB("다양한 직무와 섞이기");

	private final String label;

	@JsonCreator
	public static StudySynergy deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralExceptionHandler(StudyErrorCode.INVALID_ATTEND_STATUS));
	}
}