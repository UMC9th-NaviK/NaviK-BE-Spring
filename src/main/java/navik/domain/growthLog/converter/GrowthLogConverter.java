package navik.domain.growthLog.converter;

import org.springframework.data.domain.Page;

import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.global.dto.PageResponseDto;

public class GrowthLogConverter {

	private GrowthLogConverter() {
	}

	public static GrowthLogResponseDTO.Detail toDetail(GrowthLog growthLog) {
		return new GrowthLogResponseDTO.Detail(
			growthLog.getId(),
			growthLog.getKpiCard() == null ? null : growthLog.getKpiCard().getId(),
			growthLog.getType(),
			growthLog.getTitle(),
			growthLog.getContent(),
			growthLog.getScore(),
			growthLog.getCreatedAt(),
			growthLog.getUpdatedAt()
		);
	}

	public static GrowthLogResponseDTO.ListItem toListItem(GrowthLog growthLog) {
		return new GrowthLogResponseDTO.ListItem(
			growthLog.getId(),
			growthLog.getTitle(),
			growthLog.getContent(),
			growthLog.getCreatedAt()
		);
	}

	public static PageResponseDto<GrowthLogResponseDTO.ListItem> toPageResponse(Page<GrowthLog> page) {
		return new PageResponseDto<>(page.map(GrowthLogConverter::toListItem));
	}
}
