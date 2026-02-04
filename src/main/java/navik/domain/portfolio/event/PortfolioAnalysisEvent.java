package navik.domain.portfolio.event;

public record PortfolioAnalysisEvent(
	Long userId,
	Long portfolioId
) {
}
