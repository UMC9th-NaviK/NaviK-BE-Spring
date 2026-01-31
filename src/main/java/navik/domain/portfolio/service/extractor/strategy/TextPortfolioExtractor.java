package navik.domain.portfolio.service.extractor.strategy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.entity.InputType;

@Component
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
