package navik.global.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.dto.TokenDto;
import navik.global.auth.service.AuthService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

	private final AuthService authService;

	@PostMapping("/refresh")
	public ApiResponse<String> reissue(@CookieValue("refresh_token") String refreshToken,
		HttpServletResponse response) {

		TokenDto tokenDto = authService.reissue(refreshToken);

		// Refresh Token Cookie 설정
		ResponseCookie cookie = authService.createRefreshTokenCookie(tokenDto.getRefreshToken());
		response.addHeader("Set-Cookie", cookie.toString());

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, tokenDto.getAccessToken());
	}

	@PostMapping("/logout")
	public ApiResponse<String> logout(@RequestHeader("Authorization") String accessToken,
		@CookieValue("refresh_token") String refreshToken,
		HttpServletResponse response) {

		authService.logout(accessToken, refreshToken);

		// 쿠키 삭제 (빈 값으로 덮어쓰기)
		ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
			.httpOnly(true)
			.secure(true)
			.path("/v1/auth")
			.maxAge(0) // 만료
			.sameSite("None")
			.build();
		response.addHeader("Set-Cookie", cookie.toString());

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, "로그아웃 되었습니다.");
	}
}