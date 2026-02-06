package navik.domain.portfolio.service.extractor.strategy;

import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.entity.InputType;

public interface PortfolioTextExtractor {
	boolean supports(InputType inputType);

	String extractText(PortfolioRequestDto.Create request);
}
