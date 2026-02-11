package navik.domain.portfolio.event;

import navik.domain.portfolio.entity.AnalysisType;

public record PortfolioAnalysisEvent(
	Long userId,
	Long portfolioId,
	boolean isFallBacked,
	AnalysisType analysisType
) {
}
