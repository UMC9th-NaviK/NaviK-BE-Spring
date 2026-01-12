package navik.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendStatus {

	ACCEPTANCE("수락"),
	WAITING("대기"),
	REJECTION("거절");

	private final String label;
}
