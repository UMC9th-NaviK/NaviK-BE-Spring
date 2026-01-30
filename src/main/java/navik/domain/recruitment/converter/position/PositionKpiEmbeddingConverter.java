package navik.domain.recruitment.converter.position;

import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.PositionKpiEmbedding;

public class PositionKpiEmbeddingConverter {

	public static PositionKpiEmbedding toEntity(PositionKpi positionKpi, float[] embedding) {
		return PositionKpiEmbedding.builder()
			.positionKpi(positionKpi)
			.embedding(embedding)
			.build();
	}
}
