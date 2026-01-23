package navik.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyKpiCardDTO {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StudyKpiCardNameDTO {
		private Long kpiId;
		private String name;
	}
}
