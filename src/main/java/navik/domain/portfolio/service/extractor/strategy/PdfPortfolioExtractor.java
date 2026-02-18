package navik.domain.portfolio.service.extractor.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.entity.InputType;

@Component
@RequiredArgsConstructor
public class PdfPortfolioExtractor implements PortfolioTextExtractor {

	private final PortfolioAiClient portfolioAiClient;

	@Value("${spring.cloud.aws.s3.prefix}")
	private String S3Prefix;

	@Override
	public boolean supports(InputType inputType) {
		return inputType == InputType.PDF;
	}

	@Override
	public String extractText(PortfolioRequestDTO.Create request) {
		if (request.fileUrl().startsWith("https://"))
			return portfolioAiClient.extractTextFromPdf(S3Prefix + request.fileUrl());
		else
			return portfolioAiClient.extractTextFromPdf(request.fileUrl());
	}
}

