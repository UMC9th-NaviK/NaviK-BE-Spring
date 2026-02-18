package navik.global.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.dto.RefreshDTO;
import navik.global.auth.dto.RefreshResponseDTO;
import navik.global.auth.dto.TokenDTO;
import navik.global.auth.service.AuthService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

	private final AuthService authService;

	@Value("${spring.oauth2.cookie-domain}")
	private String cookieDomain;

	@PostMapping("/refresh")
	public ApiResponse<RefreshResponseDTO> reissue(@CookieValue("refresh_token") String refreshToken,
		HttpServletResponse response) {

		RefreshDTO refreshDTO = authService.reissue(refreshToken);

		// Refresh Token Cookie 설정
		ResponseCookie cookie = authService.createRefreshTokenCookie(refreshDTO.tokenDTO().getRefreshToken());
		response.addHeader("Set-Cookie", cookie.toString());

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, new RefreshResponseDTO(refreshDTO.tokenDTO().getAccessToken(),refreshDTO.status()));
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
			.domain(cookieDomain)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, "로그아웃 되었습니다.");
	}
}