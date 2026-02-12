package navik.domain.portfolio.worker.strategy;

import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.domain.portfolio.entity.AnalysisType;

public interface PortfolioAnalysisStrategy {
	boolean supports(AnalysisType type);

	PortfolioAiDTO.AnalyzeResponse analyze(String resumeText, Long jobId);
}
