package navik.domain.portfolio.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.dto.PortfolioResponseDTO;
import navik.domain.portfolio.entity.AnalysisType;
import navik.domain.portfolio.service.PortfolioCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/portfolios")
@Tag(name = "Portfolio V2", description = "포트폴리오 V2 API (abilities 임베딩 포함)")
public class PortfolioV2Controller {

	private final PortfolioCommandService portfolioCommandService;

	@PostMapping
	@Operation(
		summary = "포트폴리오 등록 및 분석 요청 (V2 - abilities 임베딩 포함)",
		description = """
			포트폴리오를 등록하고 AI 분석을 비동기로 요청합니다.
			V1과 동일하게 KPI 점수를 도출하며, 추가로 abilities 임베딩 값을 저장합니다.
			"""
	)
	public ApiResponse<PortfolioResponseDTO.Created> registerPortfolio(@AuthUser Long userId,
		@RequestBody @Valid PortfolioRequestDTO.Create request) {

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED,
			portfolioCommandService.createPortfolio(userId, request, AnalysisType.WITH_ABILITY));
	}
}
