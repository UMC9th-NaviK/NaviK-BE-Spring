package navik.domain.evaluation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagType {

	STRENGTH("강점태깅"),
	IMPROVEMENT("약점태깅");

	private final String label;
}
