package navik.global.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.AuthErrorCode;
import navik.global.auth.JwtUserDetails;

public class UserStatusFilter extends OncePerRequestFilter {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private static final List<String> ONBOARDING_ALLOWED_PATHS = List.of(
		"/v1/users/me/basic-info",
		"/v1/users/check-nickname",
		"/v1/jobs/**", "/v1/terms/**",
		"/v1/departments/**"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
			if ("PENDING".equals(userDetails.getStatus()) && !isOnboardingPath(request.getRequestURI())) {
				response.setContentType("application/json;charset=UTF-8");
				response.setStatus(AuthErrorCode.ONBOARDING_REQUIRED.getHttpStatus().value());

				ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(AuthErrorCode.ONBOARDING_REQUIRED);

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				response.getWriter().write(objectMapper.writeValueAsString(errorBody));
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isOnboardingPath(String requestUri) {
		return ONBOARDING_ALLOWED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
	}
}
