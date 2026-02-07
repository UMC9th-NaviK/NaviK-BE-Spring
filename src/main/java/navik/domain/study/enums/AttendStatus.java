package navik.domain.study.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum AttendStatus {

	ACCEPTANCE("수락"),
	WAITING("대기"),
	REJECTION("거절");

	private final String label;

	@JsonCreator
	public static AttendStatus deserialize(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(value))
			.findAny()
			.orElseThrow(() -> new GeneralException(StudyErrorCode.INVALID_ATTEND_STATUS));
	}
}
