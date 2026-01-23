package navik.domain.recruitment.converter.position;

import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.PositionKpiEmbedding;

public class PositionKpiEmbeddingConverter {

	public static PositionKpiEmbedding toEntity(PositionKpi positionKpi,
		RecruitmentRequestDTO.Recruitment.Position.KPI kpi) {
		return PositionKpiEmbedding.builder()
			.positionKpi(positionKpi)
			.embedding(kpi.getEmbedding())
			.build();
	}
}
