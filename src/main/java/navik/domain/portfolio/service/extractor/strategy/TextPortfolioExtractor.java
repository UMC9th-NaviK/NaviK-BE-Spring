package navik.domain.portfolio.service.extractor.strategy;

import org.springframework.stereotype.Component;

import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.entity.InputType;

@Component
public class TextPortfolioExtractor implements PortfolioTextExtractor {
	@Override
	public boolean supports(InputType inputType) {
		return inputType == InputType.TEXT;
	}

	@Override
	public String extractText(PortfolioRequestDTO.Create request) {
		return request.content();
	}
}
