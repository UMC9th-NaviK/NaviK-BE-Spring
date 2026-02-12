package navik.domain.portfolio.message;

import navik.domain.portfolio.entity.AnalysisType;

public record PortfolioAnalysisMessage(
	Long userId,
	Long portfolioId,
	String traceId,
	boolean isFallBacked,
	AnalysisType analysisType
) {
}
