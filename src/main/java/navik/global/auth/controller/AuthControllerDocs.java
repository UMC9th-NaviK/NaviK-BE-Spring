package navik.global.auth.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(summary = "토큰 재발급", description = "Cookie에 있는 Refresh Token을 이용하여 새로운 Access Token을 발급합니다.")
    ApiResponse<String> reissue(
            @Parameter(description = "Refresh Token (HttpOnly Cookie)", required = true) @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리하고 Refresh Token Cookie를 삭제합니다.")
    ApiResponse<String> logout(
            @Parameter(description = "Access Token", required = true) @RequestHeader("Authorization") String accessToken,
            @Parameter(description = "Refresh Token (HttpOnly Cookie)", required = true) @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response);

    @Operation(summary = "개발용 토큰 발급", description = "개발 환경에서만 사용 가능합니다. 특정 사용자의 Access Token과 Refresh Token을 발급합니다.")
    ApiResponse<String> createDevToken(
            @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId,
            @Parameter(description = "액세스 토큰 만료 시간", required = true) @RequestParam Long accessTokenValidityInSeconds,
            @Parameter(description = "리프레시 토큰 만료 시간", required = true) @RequestParam Long refreshTokenValidityInSeconds,
            HttpServletResponse response);
}