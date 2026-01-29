package navik.global.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.global.auth.dto.TokenDto;
import navik.global.auth.jwt.JwtTokenProvider;
import navik.global.auth.redis.RefreshToken;
import navik.global.auth.repository.RefreshTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final StringRedisTemplate redisTemplate;
	private final CustomUserDetailsService customUserDetailsService;

	@Value("${spring.jwt.refresh-token-validity-in-seconds}")
	private long refreshTokenValidityInSeconds;
	@Value("${spring.jwt.access-token-validity-in-seconds}")
	private long accessTokenValidityInSeconds;

	@Transactional
	public TokenDto reissue(String refreshToken) {
		// 1. Refresh Token 검증
		jwtTokenProvider.validateToken(refreshToken, false);

		// 2. Refresh Token 에서 사용자 ID 가져오기
		String userIdStr = jwtTokenProvider.getSubject(refreshToken);

		// 3. Redis 에서 id(userId) 를 기반으로 저장된 Refresh Token 값을 가져옴
		RefreshToken redisRefreshToken = refreshTokenRepository.findById(userIdStr)
			.orElseThrow(() -> new GeneralExceptionHandler(AuthErrorCode.INVALID_REFRESH_TOKEN));

		// 4. Refresh Token 일치하는지 검사
		if (!redisRefreshToken.getToken().equals(refreshToken)) {
			throw new GeneralExceptionHandler(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
		}

		// 5. Refresh Token & AccessToken
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userIdStr);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			userDetails.getAuthorities());
		TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication, accessTokenValidityInSeconds,
			refreshTokenValidityInSeconds);

		// 6. 리프레시 토큰 갱신 (RTR 방식)
		redisRefreshToken.updateToken(tokenDto.getRefreshToken());
		refreshTokenRepository.save(redisRefreshToken);

		return tokenDto;
	}

	@Transactional
	public void logout(String accessToken, String refreshToken) {
		// Bearer 제거
		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			accessToken = accessToken.substring(7);
		}

		// 1. Access Token 검증
		jwtTokenProvider.validateToken(accessToken, true);

		// 2. Access Token 에서 User ID 가져오기
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

		// 3. Redis 에서 해당 User ID 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
		if (refreshTokenRepository.findById(authentication.getName()).isPresent()) {
			refreshTokenRepository.deleteById(authentication.getName());
		}

		// 4. Access Token 유효시간을 가져와서 BlackList로 저장
		Long expiration = jwtTokenProvider.getExpiration(accessToken);
		redisTemplate.opsForValue()
			.set("blacklist:" + accessToken, "logout", expiration, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	public org.springframework.http.ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return org.springframework.http.ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/v1/auth")
			.maxAge(refreshTokenValidityInSeconds)
			.sameSite("None")
			.build();
	}

	@Transactional
	public TokenDto createDevToken(Long userId, Long accessTokenValidityInMs, Long refreshTokenValidityInMs) {
		// 1. 사용자 정보 로드
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId.toString());

		// 2. Authentication 객체 생성
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication, accessTokenValidityInMs,
			refreshTokenValidityInMs);

		// 4. Refresh Token Redis에 저장
		RefreshToken refreshToken = RefreshToken.builder()
			.id(userId.toString())
			.token(tokenDto.getRefreshToken())
			.build();
		refreshTokenRepository.save(refreshToken);

		return tokenDto;
	}
}
