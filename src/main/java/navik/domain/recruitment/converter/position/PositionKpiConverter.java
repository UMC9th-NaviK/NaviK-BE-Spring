package navik.domain.recruitment.converter.position;

import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;

public class PositionKpiConverter {

	public static PositionKpi toEntity(Position position, String content) {
		return PositionKpi.builder()
			.position(position)
			.content(content)
			.build();
	}
}
