package navik.global.auth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.global.auth.dto.TokenDTO;
import navik.global.auth.jwt.JwtTokenProvider;
import navik.global.auth.redis.RefreshToken;
import navik.global.auth.repository.RefreshTokenRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${spring.jwt.access-token-validity-in-seconds}")
	private long accessTokenValidityInSeconds;

	@Value("${spring.jwt.refresh-token-validity-in-seconds}")
	private long refreshTokenValidityInSeconds;

	@Value("${spring.oauth2.redirect-url}")
	private String redirectUrl;

	@Value("${spring.oauth2.cookie-domain}")
	private String cookieDomain;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		// 1. 토큰 생성
		TokenDTO tokenDto = jwtTokenProvider.generateTokenDto(authentication, accessTokenValidityInSeconds,
			refreshTokenValidityInSeconds);

		// 2. Refresh Token 저장
		saveRefreshToken(authentication, tokenDto);

		clearOldRefreshTokenCookie(response);
		// 3. Refresh Token을 HttpOnly Cookie로 설정
		setRefreshTokenCookie(response, tokenDto);

		// 리다이렉트
		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}

	private void saveRefreshToken(Authentication authentication, TokenDTO tokenDto) {
		String userId = authentication.getName();

		refreshTokenRepository.findById(userId)
			.ifPresent(refreshTokenRepository::delete);

		RefreshToken refreshToken = RefreshToken.builder()
			.id(authentication.getName()) // 사용자 ID (PK)
			.token(tokenDto.getRefreshToken())
			.build();

		refreshTokenRepository.save(refreshToken);
	}

	private void setRefreshTokenCookie(HttpServletResponse response, TokenDTO tokenDto) {

		ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenDto.getRefreshToken())
			.httpOnly(true)
			.secure(true)
			.path("/v1/auth")
			.maxAge(refreshTokenValidityInSeconds)
			.sameSite("None")
			.domain(cookieDomain)
			.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	private void clearOldRefreshTokenCookie(HttpServletResponse response) {
		ResponseCookie clearOldCookie = ResponseCookie.from("refresh_token", "")
			.httpOnly(true)
			.secure(true)
			.path("/v1/auth")
			.domain(cookieDomain)
			.maxAge(0) // 즉시 만료
			.sameSite("None")
			.build();

		response.addHeader("Set-Cookie", clearOldCookie.toString());
	}
}