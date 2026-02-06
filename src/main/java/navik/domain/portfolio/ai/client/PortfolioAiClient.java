package navik.domain.portfolio.ai.client;

import navik.domain.portfolio.dto.PortfolioAiDTO;

public interface PortfolioAiClient {

	String extractTextFromPdf(String fileUrl);

	PortfolioAiDTO.AnalyzeResponse analyzePortfolio(String portfolioText);
}
