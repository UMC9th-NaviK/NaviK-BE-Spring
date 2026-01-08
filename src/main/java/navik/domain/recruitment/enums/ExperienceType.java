package navik.domain.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceType {

	ENTRY("신입"),
	EXPERIENCED("경력");

	private final String label;
}
