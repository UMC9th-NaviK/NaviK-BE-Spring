package navik.global.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	// JWT 필터를 적용하지 않을 경로 목록
	private static final List<String> EXCLUDED_PATHS = List.of(
		"/v1/auth/refresh",
		"/dev/token", "/login/oauth2/"
	);

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		try {
			// 1. Request Header 에서 토큰을 꺼냄
			String jwt = resolveToken(request);

			// 2. validateToken 으로 토큰 유효성 검사
			// 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
			if (StringUtils.hasText(jwt)) {
				jwtTokenProvider.validateToken(jwt, true);
				Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);

		} catch (GeneralExceptionHandler e) {
			log.error("JWT 인증 실패: {}", e.getMessage());

			// JWT 검증 실패 시 직접 JSON 에러 응답 반환
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(e.getCode().getHttpStatus().value());

			ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(e.getCode());

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
			response.getWriter().write(objectMapper.writeValueAsString(errorBody));
		}
	}

	// Request Header 에서 토큰 정보를 꺼내오기
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(7);
		}
		return null;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return EXCLUDED_PATHS.stream().anyMatch(path::equals);
	}
}
