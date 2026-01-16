package navik.domain.growthLog.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AggregateUnit {
	DAY, WEEK, MONTH;

	@JsonCreator
	public static AggregateUnit from(String value) {
		return AggregateUnit.valueOf(value.toUpperCase());
	}

}