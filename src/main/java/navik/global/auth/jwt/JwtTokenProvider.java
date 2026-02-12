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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.auth.JwtUserDetails;
import navik.global.auth.dto.TokenDTO;
import navik.global.auth.exception.code.AuthErrorCode;

@Slf4j
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_TYPE = "Bearer";

	// Key -> SecretKey 타입 변경 (0.12.x 권장)
	private final SecretKey key;

	public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(Authentication authentication, long expirationTime) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		Object principal = authentication.getPrincipal();

		String status = null;
		if (principal instanceof JwtUserDetails userDetails) {
			status = userDetails.getStatus();
		}

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + expirationTime * 1000); // 밀리초 -> 초

		return Jwts.builder()
			.subject(authentication.getName())
			.claim(AUTHORITIES_KEY, authorities)
			.claim("status", status)
			.expiration(accessTokenExpiresIn)
			.signWith(key)
			.compact();
	}

	public String generateRefreshToken(Authentication authentication, long expirationTime) {
		long now = (new Date()).getTime();
		return Jwts.builder()
			.subject(authentication.getName()) // email 주소
			.expiration(new Date(now + expirationTime * 1000))
			.signWith(key)
			.compact();
	}

	public TokenDTO generateTokenDto(Authentication authentication, long accessTokenValidityInSeconds,
		long refreshTokenValidityInSeconds) {
		String accessToken = generateAccessToken(authentication, accessTokenValidityInSeconds);
		String refreshToken = generateRefreshToken(authentication, refreshTokenValidityInSeconds);

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenValidityInSeconds * 1000);

		return TokenDTO.builder()
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
			throw new GeneralException(AuthErrorCode.TOKEN_NOT_FOUND);
		}

		Collection<? extends GrantedAuthority> authorities = Arrays
			.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());

		// status claim 읽기
		String status = claims.get("status", String.class);
		Long userId = Long.parseLong(claims.getSubject());

		UserDetails principal = new JwtUserDetails(userId, status, authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public void validateToken(String token, boolean isAccessToken) {
		try {
			// [변경 5] parserBuilder() -> parser(), verifyWith(key), parseSignedClaims()
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.error("잘못된 JWT 서명입니다.");
			throw new GeneralException(AuthErrorCode.AUTH_TOKEN_INVALID);
		} catch (ExpiredJwtException e) {
			log.warn("만료된 JWT 토큰입니다.");
			throw new GeneralException(
				isAccessToken ? AuthErrorCode.AUTH_TOKEN_EXPIRED : AuthErrorCode.REFRESH_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT 토큰입니다.");
			throw new GeneralException(AuthErrorCode.AUTH_TOKEN_INVALID);
		} catch (IllegalArgumentException e) {
			log.error("JWT 토큰이 잘못되었습니다.");
			throw new GeneralException(AuthErrorCode.AUTH_TOKEN_INVALID);
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