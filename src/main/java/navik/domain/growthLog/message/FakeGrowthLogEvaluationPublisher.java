package navik.domain.growthLog.message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "sync", matchIfMissing = true)
public class FakeGrowthLogEvaluationPublisher implements GrowthLogEvaluationPublisher {

	@Override
	public void publish(GrowthLogEvaluationMessage message) {
		// TODO: Redis Stream/SQS 구현으로 교체
		// 지금은 비동기 플로우 DI/스위칭만 검증하기 위한 noop
	}

}
