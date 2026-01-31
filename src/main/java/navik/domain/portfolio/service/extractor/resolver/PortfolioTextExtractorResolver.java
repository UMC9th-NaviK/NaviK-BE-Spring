package navik.domain.portfolio.service.extractor.resolver;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.entity.InputType;
import navik.domain.portfolio.service.extractor.strategy.PdfPortfolioExtractor;
import navik.domain.portfolio.service.extractor.strategy.PortfolioTextExtractor;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Component
@RequiredArgsConstructor
public class PortfolioTextExtractorResolver {

	private final List<PdfPortfolioExtractor> extractors;

	public PortfolioTextExtractor resolve(InputType inputType) {
		return extractors.stream().filter(extractor -> extractor.supports(inputType)).findAny() // inputType은 unique
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));
	}
}
