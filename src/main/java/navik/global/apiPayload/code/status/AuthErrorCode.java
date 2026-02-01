package navik.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

	/**
	 * SecurityConfig의 AuthenticationEntryPoint에서 사용
	 * - 토큰이 없는 상태에서 인증이 필요한 엔드포인트 접근 시
	 */
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401_01", "인증되지 않은 사용자입니다."),

	/**
	 * JWT 토큰 만료
	 * - ExpiredJwtException 발생 시
	 */
	AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_02", "액세스 토큰이 만료되었습니다."),

	/**
	 * JWT 토큰 서명/형식 오류
	 * - SecurityException, MalformedJwtException, UnsupportedJwtException, IllegalArgumentException 발생 시
	 */
	AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_03", "유효하지 않은 액세스 토큰입니다."),

	/**
	 * JWT 토큰에 권한 정보 없음
	 * - getAuthentication() 에서 authorities claim 없을 때
	 */
	TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_04", "인증 토큰이 존재하지 않습니다."),

	/**
	 * 리프레시 토큰 검증 실패
	 */
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_05", "유효하지 않은 리프레시 토큰입니다."),

	/**
	 * 쿠키에 리프레시 토큰 없음
	 */
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_06", "리프레시 토큰이 존재하지 않습니다."),

	/**
	 * 리프레시 토큰 만료
	 */
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_07", "리프레시 토큰이 만료되었습니다."),

	/**
	 * Redis에 저장된 리프레시 토큰과 불일치
	 */
	REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_401_08", "저장된 리프레시 토큰과 일치하지 않습니다."),

	/**
	 * 인증은 되었으나 리소스 접근 권한 없음
	 * - 예: USER 권한으로 ADMIN 전용 API 접근 시
	 */
	AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403_01", "접근 권한이 없습니다."),

	/**
	 * 온보딩 미완료 사용자의 일반 API 접근 시
	 * - PENDING 상태 사용자가 온보딩 허용 경로 외 API 접근 시
	 */
	ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN, "AUTH_403_02", "온보딩이 완료되지 않았습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
