package navik.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudySynergy {

	SAME_JOB("비슷한 직무끼리 모이기"),
	DIVERSE_JOB("다양한 직무와 섞이기");

	private final String label;

}