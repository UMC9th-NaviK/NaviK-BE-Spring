package navik.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyRole {

	STUDY_LEADER("스터디장"),
	STUDY_MEMBER("스터디원");

	private final String label;
}
