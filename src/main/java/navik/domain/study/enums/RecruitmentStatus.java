package navik.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStatus {

	RECURRING("모집중"),
	IN_PROGRESS("진행중"),
	CLOSED("종료");

	private final String label;
}
