package navik.domain.growthLog.message;

public interface GrowthLogEvaluationPublisher {
	void publish(GrowthLogEvaluationMessage message);
}