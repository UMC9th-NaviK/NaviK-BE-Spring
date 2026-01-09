package navik.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobType {

	PRODUCT_MANAGER("프로덕트 매니저"),
	PRODUCT_DESIGNER("프로덕트 디자이너"),
	FRONTEND_DEVELOPER("프론트엔드 개발자"),
	BACKEND_DEVELOPER("백엔드 개발자");

	private final String label;
}