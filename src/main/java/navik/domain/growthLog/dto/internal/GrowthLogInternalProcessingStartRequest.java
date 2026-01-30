package navik.domain.growthLog.dto.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrowthLogInternalProcessingStartRequest(
	@NotNull Long userId,
	String traceId,
	@NotBlank String processingToken
) {
}