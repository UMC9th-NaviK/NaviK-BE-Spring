package navik.domain.growthLog.ai.client;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.AiServerProperties;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.GrowthLogEvaluationContext;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class AiServerGrowthLogAiClient implements GrowthLogAiClient {

	private final WebClient aiWebClient;
	private final AiServerProperties props;

	private static final String EVALUATE_USER_INPUT_PATH =
		"/v1/growth-logs/evaluate/user-input";

	@Override
	public GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		GrowthLogEvaluationContext context
	) {

		try {
			return aiWebClient.post()
				.uri(EVALUATE_USER_INPUT_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(
					new GrowthLogAiRequestDTO.EvaluateUserInputRequest(userId, context)
				)
				.retrieve()
				.bodyToMono(GrowthLogEvaluationResult.class)
				.timeout(Duration.ofSeconds(10))
				.block();
		} catch (Exception e) {
			throw new GeneralExceptionHandler(
				GrowthLogErrorCode.AI_EVALUATION_FAILED
			);
		}
	}

}
