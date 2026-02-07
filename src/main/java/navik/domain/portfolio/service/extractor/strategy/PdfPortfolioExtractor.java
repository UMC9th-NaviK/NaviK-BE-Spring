package navik.domain.portfolio.service.extractor.strategy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.entity.InputType;

@Component
@RequiredArgsConstructor
public class PdfPortfolioExtractor implements PortfolioTextExtractor {

	private final PortfolioAiClient portfolioAiClient;

	@Override
	public boolean supports(InputType inputType) {
		return inputType == InputType.PDF;
	}

	@Override
	public String extractText(PortfolioRequestDTO.Create request) {
		return portfolioAiClient.extractTextFromPdf(request.fileUrl());
	}
}

