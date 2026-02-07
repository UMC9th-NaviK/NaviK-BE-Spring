package navik.domain.growthLog.converter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.global.dto.PageResponseDTO;
import navik.global.dto.SliceResponseDTO;

public class GrowthLogConverter {

	private GrowthLogConverter() {
	}

	public static PageResponseDTO<GrowthLogResponseDTO.ListItem> toPageResponse(Page<GrowthLog> page) {
		return PageResponseDTO.of(
			page.map(gl -> new GrowthLogResponseDTO.ListItem(
				gl.getId(),
				gl.getTitle(),
				gl.getContent(),
				gl.getTotalDelta(),
				gl.getCreatedAt()
			))
		);
	}

	public static GrowthLogResponseDTO.Detail toDetail(GrowthLog gl) {
		return new GrowthLogResponseDTO.Detail(
			gl.getId(),
			gl.getType(),
			gl.getTitle(),
			gl.getContent(),
			gl.getTotalDelta(),
			gl.getStatus(),
			gl.getCreatedAt(),
			gl.getKpiLinks().stream()
				.map(l -> new GrowthLogResponseDTO.KpiLinkItem(
					l.getKpiCard().getId(),
					l.getKpiCard().getName(),
					l.getDelta()
				))
				.toList()
		);
	}

	public static SliceResponseDTO<GrowthLogResponseDTO.ListItem> toSliceResponse(Slice<GrowthLog> slice) {
		return SliceResponseDTO.of(
			slice.map(gl -> new GrowthLogResponseDTO.ListItem(
				gl.getId(),
				gl.getTitle(),
				gl.getContent(),
				gl.getTotalDelta(),
				gl.getCreatedAt()
			))
		);
	}
}
