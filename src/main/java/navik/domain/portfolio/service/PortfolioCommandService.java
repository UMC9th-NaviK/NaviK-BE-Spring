package navik.domain.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.InputType;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;
	private final UserQueryService userQueryService;
	private final PortfolioAiClient portfolioAiClient;

	public PortfolioResponseDto.Created createPortfolio(Long userId, PortfolioRequestDto.Create request) {
		User user = userQueryService.getUser(userId);

		PortfolioStatus status = switch (request.inputType()) {
			case PDF -> PortfolioStatus.PDF_PENDING;
			case TEXT -> PortfolioStatus.TEXT_INPUT;
		};

		Portfolio portfolio = Portfolio.builder()
			.inputType(request.inputType())
			.content(request.content())
			.fileUrl(request.fileUrl())
			.status(status)
			.user(user)
			.build();

		if(portfolio.getInputType().equals(InputType.PDF)){
			analyzePortfolioPdf(portfolio);
		}
		portfolioRepository.save(portfolio);

		return new PortfolioResponseDto.Created(portfolio.getId(), request.inputType());
	}

	public void analyzePortfolioPdf(Portfolio portfolio) {
		String extractedText = portfolioAiClient.extractTextFromPdf(portfolio.getFileUrl());
		portfolio.completeOcr(extractedText);
	}
}
