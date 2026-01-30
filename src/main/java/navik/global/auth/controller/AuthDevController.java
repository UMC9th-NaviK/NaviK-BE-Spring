package navik.global.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.dto.TokenDto;
import navik.global.auth.service.AuthService;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile(value = {"dev"})
@Validated
public class AuthDevController implements AuthDevControllerDocs {
	private final AuthService authService;

	@PostMapping("/token")
	public ApiResponse<String> createDevToken(
		@RequestParam Long userId,
		@RequestParam @PositiveOrZero Long accessTokenValidityInSeconds,
		@RequestParam @PositiveOrZero Long refreshTokenValidityInSeconds,
		HttpServletResponse response) {

		TokenDto tokenDto = authService.createDevToken(userId, accessTokenValidityInSeconds,
			refreshTokenValidityInSeconds);

		// Refresh Token Cookie 설정
		ResponseCookie cookie = authService.createRefreshTokenCookie(tokenDto.getRefreshToken());
		response.addHeader("Set-Cookie", cookie.toString());

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, tokenDto.getAccessToken());
	}
}
