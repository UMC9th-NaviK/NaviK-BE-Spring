package navik.domain.recruitment.dto.recruitment;

import lombok.Builder;
import lombok.Getter;

public class RecruitmentResponseDTO {

	@Getter
	@Builder
	public static class RecommendedPost {
		private Long id;
		private String postId;
		private String link;
		private String companyLogo;
		private String companyName;
		private String companySize;
		private long dDay;
		private String title;
		private String workPlace;
		private String experience;
		private String employment;
		private boolean isRecommend;
		private String aiSummary;
	}
}
