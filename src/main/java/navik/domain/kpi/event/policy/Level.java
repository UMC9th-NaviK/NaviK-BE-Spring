package navik.domain.kpi.event.policy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Level {

	LEVEL_1(1, 0L, "성장을 시작한 단계입니다."),
	LEVEL_2(2, 201L, "기초 역량을 쌓고 있습니다."),
	LEVEL_3(3, 451L, "꾸준히 발전하고 있습니다."),
	LEVEL_4(4, 751L, "안정적인 성장 흐름을 보이고 있습니다."),
	LEVEL_5(5, 1101L, "상위권으로 도약 중입니다."),
	LEVEL_6(6, 1551L, "높은 수준의 역량을 갖추고 있습니다."),
	LEVEL_7(7, 2101L, "전문가 단계에 가까워지고 있습니다."),
	LEVEL_8(8, 2801L, "상위 수준의 성장입니다."),
	LEVEL_9(9, 3701L, "최상위권에 도달했습니다."),
	LEVEL_10(10, 4801L, "최고 레벨에 도달했습니다.");

	private final int value;
	private final long minScore;
	private final String description;

	public static Level fromScore(Long totalScore) {
		if (totalScore == null) {
			return LEVEL_1;
		}

		Level[] levels = values();
		for (int i = levels.length - 1; i >= 0; i--) {
			if (totalScore >= levels[i].minScore) {
				return levels[i];
			}
		}
		return LEVEL_1;
	}
}
