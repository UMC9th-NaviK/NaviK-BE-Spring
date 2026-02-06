package navik.domain.study.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStatus {

	RECURRING("모집중"),
	IN_PROGRESS("진행중"),
	CLOSED("종료");

	private final String label;

	@JsonCreator
	public static RecruitmentStatus deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralExceptionHandler(StudyErrorCode.INVALID_RECRUITMENT_STATUS));
	}
}
