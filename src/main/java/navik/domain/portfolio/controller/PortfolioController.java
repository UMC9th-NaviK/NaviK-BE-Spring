package navik.domain.portfolio.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.dto.PortfolioResponseDTO;
import navik.domain.portfolio.service.PortfolioCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolios")
public class PortfolioController implements PortfolioControllerDocs {

	private final PortfolioCommandService portfolioCommandService;

	@PostMapping
	public ApiResponse<PortfolioResponseDTO.Created> registerPortfolio(
		@AuthUser Long userId,
		@RequestBody @Valid PortfolioRequestDTO.Create request) {

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED,
			portfolioCommandService.createPortfolio(userId, request));
	}

	@PostMapping("/{portfolioId}/additional-info")
	public ApiResponse<PortfolioResponseDTO.AdditionalInfoSubmitted> submitAdditionalInfo(
		@AuthUser Long userId,
		@PathVariable Long portfolioId,
		@RequestBody @Valid PortfolioRequestDTO.AdditionalInfo request) {

		return ApiResponse.onSuccess(GeneralSuccessCode._OK,
			portfolioCommandService.submitAdditionalInfo(userId, portfolioId, request));
	}
}
