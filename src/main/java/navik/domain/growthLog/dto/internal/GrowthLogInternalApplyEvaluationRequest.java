package navik.domain.growthLog.dto.internal;

import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult.*;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrowthLogInternalApplyEvaluationRequest(
	@NotNull Long userId,
	String traceId,
	@NotBlank String processingToken,

	@NotBlank String title,
	@NotBlank String content,

	@NotNull List<@Valid KpiDelta> kpis,
	List<@Valid AbilityResult> abilities
) {
	public record KpiDelta(
		@NotNull Long kpiCardId,
		@NotNull Integer delta) {
	}

}