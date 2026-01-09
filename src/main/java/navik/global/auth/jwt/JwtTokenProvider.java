package navik.global.auth.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.global.auth.dto.TokenDto;

@Slf4j
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_TYPE = "Bearer";
	private final long accessTokenValidityInMilliseconds;
	private final long refreshTokenValidityInMilliseconds;

	// Key -> SecretKey 타입 변경 (0.12.x 권장)
	private final SecretKey key;

	public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey,
		@Value("${spring.jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
		@Value("${spring.jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
		this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
		this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);

		return Jwts.builder()
			.subject(authentication.getName())
			.claim(AUTHORITIES_KEY, authorities)
			.expiration(accessTokenExpiresIn)
			.signWith(key)
			.compact();
	}

	public String generateRefreshToken(Authentication authentication) {
		long now = (new Date()).getTime();
		return Jwts.builder()
			.subject(authentication.getName()) // email 주소
			.expiration(new Date(now + refreshTokenValidityInMilliseconds))
			.signWith(key)
			.compact();
	}

	public TokenDto generateTokenDto(Authentication authentication) {
		String accessToken = generateAccessToken(authentication);
		String refreshToken = generateRefreshToken(authentication);

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);

		return TokenDto.builder()
			.grantType(BEARER_TYPE)
			.accessToken(accessToken)
			.accessTokenExpiresIn(accessTokenExpiresIn.getTime())
			.refreshToken(refreshToken)
			.build();
	}

	public Authentication getAuthentication(String accessToken) {
		// 토큰 복호화
		Claims claims = parseClaims(accessToken);

		if (claims.get(AUTHORITIES_KEY) == null) {
			throw new GeneralExceptionHandler(AuthErrorCode.TOKEN_NOT_FOUND);
		}

		Collection<? extends GrantedAuthority> authorities = Arrays
			.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());

		UserDetails principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public boolean validateToken(String token) {
		try {
			// [변경 5] parserBuilder() -> parser(), verifyWith(key), parseSignedClaims()
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.error("잘못된 JWT 서명입니다.", e);
			throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
		} catch (ExpiredJwtException e) {
			log.error("만료된 JWT 토큰입니다.", e);
			throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT 토큰입니다.", e);
			throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
		} catch (IllegalArgumentException e) {
			log.error("JWT 토큰이 잘못되었습니다.", e);
			throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
		}
	}

	public Long getExpiration(String accessToken) {
		// accessToken 남은 유효시간
		Date expiration = parseClaims(accessToken).getExpiration();
		// 현재 시간
		Long now = new Date().getTime();
		return (expiration.getTime() - now);
	}

	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	public String getSubject(String token) {
		return parseClaims(token).getSubject();
	}
}