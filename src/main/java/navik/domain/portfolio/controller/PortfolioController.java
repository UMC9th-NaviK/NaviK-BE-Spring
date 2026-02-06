package navik.domain.portfolio.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.service.PortfolioCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolios")
public class PortfolioController implements PortfolioControllerDocs {

	private final PortfolioCommandService portfolioCommandService;

	@PostMapping
	public ApiResponse<PortfolioResponseDto.Created> registerPortfolio(
		@AuthUser Long userId,
		@RequestBody @Valid PortfolioRequestDto.Create request) {

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED,
			portfolioCommandService.createPortfolio(userId, request));
	}
}
