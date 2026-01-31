package navik.domain.portfolio.ai.client;

import navik.domain.portfolio.dto.PortfolioAiDto;

public interface PortfolioAiClient {

	String extractTextFromPdf(String fileUrl);

	PortfolioAiDto.AnalyzeResponse analyzePortfolio(String portfolioText);
}
