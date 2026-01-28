package navik.domain.growthLog.message;

public record GrowthLogEvaluationMessage(
	Long userId,
	Long growthLogId,
	String traceId,
	String processingToken
) {
}