package navik.domain.portfolio.worker.strategy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.domain.portfolio.entity.AnalysisType;

@Component
@RequiredArgsConstructor
public class AbilityAnalysisStrategy implements PortfolioAnalysisStrategy {

	private final PortfolioAiClient portfolioAiClient;

	@Override
	public boolean supports(AnalysisType type) {
		return type == AnalysisType.WITH_ABILITY;
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyze(String resumeText, Long jobId) {
		return portfolioAiClient.analyzeAbilities(resumeText, jobId);
	}
}
