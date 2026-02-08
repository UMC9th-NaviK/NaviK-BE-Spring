package navik.domain.portfolio.ai.client;

import navik.domain.portfolio.dto.PortfolioAiDTO;

public interface PortfolioAiClient {

	String extractTextFromPdf(String fileUrl);

	PortfolioAiDTO.AnalyzeResponse analyzePortfolio(String portfolioText, Long jobId);

	PortfolioAiDTO.AnalyzeResponse analyzeWithFallback(Long jobId, Integer qB1, Integer qB2, Integer qB3, Integer qB4, Integer qB5);
}
