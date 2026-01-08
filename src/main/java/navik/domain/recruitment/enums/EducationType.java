package navik.domain.recruitment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationType {

	HIGH_SCHOOL("고등학교 졸업"),
	ASSOCIATE("전문대 졸업"),
	BACHELOR("4년제 대학 졸업"),
	MASTER("석사 졸업"),
	DOCTOR("박사 졸업");

	private final String label;
}
