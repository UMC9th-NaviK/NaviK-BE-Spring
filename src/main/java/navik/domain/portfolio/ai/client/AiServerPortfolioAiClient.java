package navik.domain.portfolio.ai.client;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioAiDto;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class AiServerPortfolioAiClient implements PortfolioAiClient {

	private final WebClient aiWebClient;

	private static final String OCR_PATH = "/v1/portfolios/ocr";

	@Override
	public String extractTextFromPdf(String fileUrl) {
		try {
			PortfolioAiDto.OcrResponse response = aiWebClient.post()
				.uri(OCR_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDto.OcrRequest(fileUrl))
				.retrieve()
				.bodyToMono(PortfolioAiDto.OcrResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();

			return response.content();
		} catch (Exception e) {
			throw new GeneralExceptionHandler(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}
}
