package navik.domain.growthLog.ai.client;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.ai.AiServerProperties;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.GrowthLogEvaluationContext;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class AiServerGrowthLogAiClient implements GrowthLogAiClient {

	private final WebClient aiWebClient;
	private final AiServerProperties props;

	@Override
	public GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		GrowthLogEvaluationContext context
	) {

		try {
			return aiWebClient.post()
				.uri(props.evaluateUserInputPath())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(
					new GrowthLogAiRequestDTO.EvaluateUserInputRequest(userId, context)
				)
				.retrieve()
				.bodyToMono(GrowthLogEvaluationResult.class)
				.timeout(Duration.ofSeconds(props.timeoutSeconds() != null ? props.timeoutSeconds() : 10))
				.block();
		} catch (WebClientResponseException e) {
			log.error("[AI] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new GeneralException(GrowthLogErrorCode.AI_EVALUATION_FAILED);
		}
	}

}
