package navik.domain.kpi.event.policy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LevelPolicy {

	// 총 점수로 레벨 반환
	public int calculateLevel(Long totalScore) {
		if (totalScore == null) {
			log.warn("totalScore is null.");
			return 0;
		}
		return Level.fromScore(totalScore).getValue();
	}
}
