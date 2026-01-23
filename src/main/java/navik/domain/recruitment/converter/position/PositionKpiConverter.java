package navik.domain.recruitment.converter.position;

import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;

public class PositionKpiConverter {

	public static PositionKpi toEntity(Position position, RecruitmentRequestDTO.Recruitment.Position.KPI kpi) {
		return PositionKpi.builder()
			.position(position)
			.content(kpi.getKpi())
			.build();
	}
}
