package navik.domain.portfolio.service.extractor.strategy;

import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.entity.InputType;

public interface PortfolioTextExtractor {
	boolean supports(InputType inputType);

	String extractText(PortfolioRequestDTO.Create request);
}
