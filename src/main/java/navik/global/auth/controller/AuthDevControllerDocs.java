package navik.global.auth.controller;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.PositiveOrZero;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Dev", description = "개발환경 전용 API")
public interface AuthDevControllerDocs {

	@Operation(summary = "개발용 토큰 발급", description = "개발 환경에서만 사용 가능합니다. 특정 사용자의 Access Token과 Refresh Token을 발급합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
		})
	ApiResponse<String> createDevToken(
		@RequestParam @Parameter(description = "사용자 ID", required = true) Long userId,
		@RequestParam @PositiveOrZero @Parameter(description = "액세스 토큰 만료 시간", required = true) Long accessTokenValidityInSeconds,
		@RequestParam @PositiveOrZero @Parameter(description = "리프레시 토큰 만료 시간", required = true) Long refreshTokenValidityInSeconds,
		HttpServletResponse response);
}
