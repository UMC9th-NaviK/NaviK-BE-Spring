package navik.domain.recruitment.converter;

import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;

public class PositionKpiConverter {
	
	public static PositionKpi toEntity(Position position, String content, float[] embedding) {
		return PositionKpi.builder()
			.position(position)
			.content(content)
			.embedding(embedding)
			.build();
	}
}
