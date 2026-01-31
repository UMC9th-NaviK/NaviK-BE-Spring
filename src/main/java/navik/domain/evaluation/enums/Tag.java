package navik.domain.evaluation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}
