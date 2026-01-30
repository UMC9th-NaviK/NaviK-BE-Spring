package navik.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.auth.handler.OAuth2SuccessHandler;
import navik.global.auth.jwt.JwtAuthenticationFilter;
import navik.global.auth.jwt.JwtTokenProvider;
import navik.global.auth.service.CustomOAuth2UserService;
import navik.global.enums.SecurityPermitPath;

@Configuration
@EnableWebSecurity
@Profile("!ci")
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityPermitPath.STATIC.getPaths()).permitAll()
				.requestMatchers(SecurityPermitPath.SWAGGER.getPaths()).permitAll()
				.requestMatchers(SecurityPermitPath.AUTH.getPaths()).permitAll()
				.requestMatchers(SecurityPermitPath.S3.getPaths()).permitAll()
				// 5. 개발환경 전용
				.requestMatchers("/dev/**").permitAll()

				// 그 외 모든 요청은 인증 필요
				.anyRequest().authenticated())

			// 인증되지 않은 사용자의 접근 시 401 JSON 응답 반환
			// (토큰이 없는 상태에서 인증 필요 엔드포인트 접근)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(AuthErrorCode.UNAUTHORIZED.getHttpStatus().value());

					ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(AuthErrorCode.UNAUTHORIZED);

					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
					response.getWriter().write(objectMapper.writeValueAsString(errorBody));
				}))

			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(oAuth2SuccessHandler))

			// JWT 필터 배치
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
