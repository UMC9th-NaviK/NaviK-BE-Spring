package navik.domain.portfolio.ai.client;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerPortfolioAiClient implements PortfolioAiClient {

	private final WebClient aiWebClient;
	private final WebClient ocrWebClient;

	private static final String OCR_PATH = "/ocr/pdf";
	private static final String ANALYZE_PATH_PREFIX = "/api/kpi/analyze/";
	private static final String ABILITIES_PATH_PREFIX = "/api/kpi/analyze/abilities/";
	private static final String FALLBACK_PATH_PREFIX = "/api/kpi/fallback/";

	private static final Map<Long, String> JOB_NAME_MAP = Map.of(1L, "pm", 2L, "designer", 3L, "frontend", 4L, "backend");

	@Override
	public String extractTextFromPdf(String fileUrl) {
		try {
			PortfolioAiDTO.OcrResponse response = ocrWebClient.post()
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
			throw new GeneralException(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyzePortfolio(String resumeText, Long jobId) {
		try {
			return aiWebClient.post()
				.uri(ANALYZE_PATH_PREFIX + JOB_NAME_MAP.get(jobId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.AnalyzeRequest(resumeText))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.AnalyzeResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();
		} catch (Exception e) {
			throw new GeneralException(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyzeAbilities(String resumeText, Long jobId) {
		try {
			return aiWebClient.post()
				.uri(ABILITIES_PATH_PREFIX + JOB_NAME_MAP.get(jobId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.AnalyzeRequest(resumeText))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.AnalyzeResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();
		} catch (Exception e) {
			throw new GeneralException(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}

	@Override
	public PortfolioAiDTO.AnalyzeResponse analyzeWithFallback(Long jobId, Integer qB1, Integer qB2, Integer qB3,
		Integer qB4, Integer qB5) {
		try {
			return aiWebClient.post()
				.uri(FALLBACK_PATH_PREFIX + JOB_NAME_MAP.get(jobId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new PortfolioAiDTO.FallbackRequest(qB1, qB2, qB3, qB4, qB5))
				.retrieve()
				.bodyToMono(PortfolioAiDTO.AnalyzeResponse.class)
				.timeout(Duration.ofSeconds(30))
				.block();
		} catch (Exception e) {
			throw new GeneralException(GeneralErrorCode.EXTERNAL_API_ERROR);
		}
	}
}
