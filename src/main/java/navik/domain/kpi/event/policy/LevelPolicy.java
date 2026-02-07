package navik.domain.kpi.event.policy;

import org.springframework.stereotype.Component;

@Component
public class LevelPolicy {
	public int calculateLevel(Long totalScore) {
		if (totalScore >= 4801) return 10;
		if (totalScore >= 3701) return 9;
		if (totalScore >= 2801) return 8;
		if (totalScore >= 2101) return 7;
		if (totalScore >= 1551) return 6;
		if (totalScore >= 1101) return 5;
		if (totalScore >= 751)  return 4;
		if (totalScore >= 451)  return 3;
		if (totalScore >= 201)  return 2;
		return 1;
	}
}
