package navik.domain.portfolio.message;

public record PortfolioAnalysisMessage(
	Long userId,
	Long portfolioId,
	String traceId,
	boolean isFallBacked
) {
}
