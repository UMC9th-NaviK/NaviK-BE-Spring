package navik.domain.portfolio.ai.client;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Component
@RequiredArgsConstructor
public class AiServerPortfolioAiClient implements PortfolioAiClient {

	private final WebClient aiWebClient;

	private static final String OCR_PATH = "/ocr/pdf";
	private static final String ANALYZE_PATH = "/api/kpi/analyze/backend";
	private static final String FALLBACK_PATH_PREFIX = "/api/kpi/fallback/";

	@Override
	public String extractTextFromPdf(String fileUrl) {
		try {
			PortfolioAiDTO.OcrResponse response = aiWebClient.post()
				.uri(OCR_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.OcrRequest(fileUrl))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.OcrResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();

			return response.text();
		} catch (Exception e) {
			throw new GeneralExceptionHandler(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyzePortfolio(String resumeText) {
		try {
			return aiWebClient.post()
				.uri(ANALYZE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.AnalyzeRequest(resumeText))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.AnalyzeResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();
		} catch (Exception e) {
			throw new GeneralExceptionHandler(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyzeWithFallback(
		String jobName,
		Integer qB1,
		Integer qB2,
		Integer qB3,
		Integer qB4,
		Integer qB5
	) {
		try {
			return aiWebClient.post()
				.uri(FALLBACK_PATH_PREFIX + jobName)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.FallbackRequest(qB1, qB2, qB3, qB4, qB5))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.AnalyzeResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();
		} catch (Exception e) {
			throw new GeneralExceptionHandler(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}
}
