package navik.domain.evaluation.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.exception.code.EvaluationErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum Tag {

	COMMUNICATION("협업&커뮤니케이션"),
	EXECUTION("책임감&실행력"),
	PROBLEM_SOLVING("문제해결&주도성"),
	GROWTH("학습태도&성장성"),
	EXPERTISE("기여도&전문성"),
	LEADERSHIP("리더쉽&조직화");

	private final String label;

	@JsonCreator
	public static Tag deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralException(EvaluationErrorCode.TAG_NOT_FOUND));
	}
}
