package navik.domain.evaluation.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.exception.code.EvaluationErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum TagType {

	STRENGTH("강점태깅"),
	IMPROVEMENT("약점태깅");

	private final String label;

	@JsonCreator
	public static TagType deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralException(EvaluationErrorCode.INVALID_TAG_TYPE));
	}
}
