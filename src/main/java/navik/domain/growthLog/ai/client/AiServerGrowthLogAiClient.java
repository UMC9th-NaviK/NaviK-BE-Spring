package navik.domain.growthLog.ai.client;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.GrowthLogEvaluationContext;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class AiServerGrowthLogAiClient implements GrowthLogAiClient {

	private static final String EVALUATE_PATH = "/v1/growth-logs/evaluate/user-input";
	private static final Integer TIMEOUT_SECONDS = 60;

	private final WebClient ocrWebClient;

	@Override
	public GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		Long jobId,
		Integer levelValue,
		GrowthLogEvaluationContext context
	) {

		try {
			return ocrWebClient.post()
				.uri(EVALUATE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(
					new GrowthLogAiRequestDTO.EvaluateUserInputRequest(userId, jobId, levelValue, context)
				)
				.retrieve()
				.bodyToMono(GrowthLogEvaluationResult.class)
				.timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
				.block();
		} catch (WebClientResponseException e) {
			log.error("[AI] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new GeneralException(GrowthLogErrorCode.AI_EVALUATION_FAILED);
		}
	}

}
