package navik.domain.recruitment.dto;

import lombok.Builder;
import lombok.Getter;

public class RecruitmentResponseDTO {

	@Getter
	@Builder
	public static class RecommendPost {
		private String companyLogo;
		private String companyName;
		private String companySize;
		private Integer deadline;
		private boolean isSaved;
		private String title;
		private String workPlace;
		private String experience;
		private String employment;
		private String link;
	}
}
