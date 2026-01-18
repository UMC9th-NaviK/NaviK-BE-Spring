package navik.domain.growthLog.converter;

import org.springframework.data.domain.Page;

import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.global.dto.PageResponseDto;

public class GrowthLogConverter {

	private GrowthLogConverter() {
	}

	public static PageResponseDto<GrowthLogResponseDTO.ListItem> toPageResponse(Page<GrowthLog> page) {
		return PageResponseDto.of(
			page.map(gl -> new GrowthLogResponseDTO.ListItem(
				gl.getId(),
				gl.getTitle(),
				gl.getContent(),
				gl.getTotalDelta(),
				gl.getCreatedAt()
			))
		);
	}
}
