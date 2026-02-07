package navik.domain.portfolio.service.extractor.resolver;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.entity.InputType;
import navik.domain.portfolio.service.extractor.strategy.PortfolioTextExtractor;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Component
@RequiredArgsConstructor
public class PortfolioTextExtractorResolver {

	private final List<PortfolioTextExtractor> extractors;

	public PortfolioTextExtractor resolve(InputType inputType) {
		return extractors.stream().filter(extractor -> extractor.supports(inputType)).findAny() // inputType은 unique
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.COMMENT_NOT_FOUND));
	}
}
