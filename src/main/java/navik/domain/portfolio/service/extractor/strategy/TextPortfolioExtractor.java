package navik.domain.portfolio.service.extractor.strategy;

import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.entity.InputType;

public class TextPortfolioExtractor implements PortfolioTextExtractor {
	@Override
	public boolean supports(InputType inputType) {
		return inputType == InputType.TEXT;
	}

	@Override
	public String extractText(PortfolioRequestDto.Create request) {
		return request.content();
	}
}
