package navik.domain.level.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Level {

	LEVEL_1(1, 0L),
	LEVEL_2(2, 201L),
	LEVEL_3(3, 451L),
	LEVEL_4(4, 751L),
	LEVEL_5(5, 1101L),
	LEVEL_6(6, 1551L),
	LEVEL_7(7, 2101L),
	LEVEL_8(8, 2801L),
	LEVEL_9(9, 3701L),
	LEVEL_10(10, 4801L);

	private final int value;
	private final long minScore;

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
