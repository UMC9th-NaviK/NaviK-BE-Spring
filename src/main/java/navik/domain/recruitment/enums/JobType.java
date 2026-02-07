package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum JobType {

	PM("프로덕트 매니저"),
	DESIGNER("프로덕트 디자이너"),
	FRONTEND("프론트엔드 개발자"),
	BACKEND("백엔드 개발자");

	private final String label;

	public static JobType getByLabel(String label) {
		for (JobType jobType : JobType.values()) {
			if (jobType.getLabel().equals(label)) {
				return jobType;
			}
		}
		return null;
	}

	@JsonCreator
	public static JobType deserialize(String jobType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(jobType))
			.findAny()
			.orElseThrow(() -> new GeneralException(RecruitmentErrorCode.JOB_TYPE_NOT_FOUND));
	}
}
