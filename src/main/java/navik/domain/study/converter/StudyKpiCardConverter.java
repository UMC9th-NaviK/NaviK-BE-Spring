package navik.domain.study.converter;

import java.util.List;

import navik.domain.kpi.entity.KpiCard;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.global.dto.CursorResponseDto;

public class StudyKpiCardConverter {

	public static StudyKpiCardDTO.StudyKpiCardNameDTO toKpiCardNameDTO(KpiCard kpiCard) {
		return StudyKpiCardDTO.StudyKpiCardNameDTO.builder()
			.kpiId(kpiCard.getId())
			.name(kpiCard.getName())
			.build();
	}

	public static CursorResponseDto<StudyKpiCardDTO.StudyKpiCardNameDTO> toKpiCardNameListDTO(
		List<KpiCard> kpiCardList,
		boolean hasNext,
		String nextCursor
	) {
		List<StudyKpiCardDTO.StudyKpiCardNameDTO> content = kpiCardList.stream()
			.map(StudyKpiCardConverter::toKpiCardNameDTO)
			.toList();

		return CursorResponseDto.of(content, hasNext, nextCursor);
	}
}
