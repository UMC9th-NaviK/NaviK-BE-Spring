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

	// 총 점수로 레벨 설명 반환
	public String getDescription(Long totalScore) {
		if (totalScore == null) {
			log.warn("totalScore is null.");
			return "";
		}
		return Level.fromScore(totalScore).getDescription();
	}

	// 총 점수로 레벨 퍼센트 반환
	public int calculatePercentage(Long totalScore) {

		if (totalScore == null || totalScore <= 0) {
			return 0;
		}

		Level current = Level.fromScore(totalScore);
		Level[] levels = Level.values();

		// 최고 레벨이면 100%
		if (current.ordinal() == levels.length - 1) {
			return 100;
		}

		Level next = levels[current.ordinal() + 1];

		long currentMin = current.getMinScore();
		long nextMin = next.getMinScore();

		if (nextMin <= currentMin) {
			log.warn("Invalid level range. current={}, currentMin={}, nextMin={}",
				current, currentMin, nextMin);
			return 0;
		}

		double progress =
			(double)(totalScore - currentMin) / (nextMin - currentMin);

		int percentage = (int)(progress * 100);

		return Math.min(100, Math.max(0, percentage));

	}

}
