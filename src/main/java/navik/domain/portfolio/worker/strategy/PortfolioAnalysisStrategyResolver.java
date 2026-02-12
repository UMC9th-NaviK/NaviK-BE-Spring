package navik.domain.portfolio.worker.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.entity.AnalysisType;

@Component
@RequiredArgsConstructor
public class PortfolioAnalysisStrategyResolver {

	private final List<PortfolioAnalysisStrategy> strategies;

	public PortfolioAnalysisStrategy resolve(AnalysisType analysisType) {
		return strategies.stream()
			.filter(strategy -> strategy.supports(analysisType))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("No strategy found for analysisType: " + analysisType));
	}
}
