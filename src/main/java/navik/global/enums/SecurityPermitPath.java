package navik.global.enums;

import lombok.Getter;

@Getter
public enum SecurityPermitPath {

	// 1. 공통 정적 리소스 및 H2 콘솔
	STATIC("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/h2-console/**"),

	// 2. Swagger UI (개발 환경)
	SWAGGER("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-resources/**"),

	// 3. 인증/인가 관련 엔드포인트
	AUTH("/v1/auth/**", "/oauth2/**", "/login/oauth2/**", "/api/notion/oauth/callback"),

	// 4. S3 관련
	S3("/v1/s3/**"),

	// 5. 개발 환경
	DEV("/dev/**");

	private final String[] paths;

	SecurityPermitPath(String... paths) {
		this.paths = paths;
	}
}
