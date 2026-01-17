package navik.domain.recruitment.dto.recruitment;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

public class RecruitmentResponseDTO {

	@Getter
	@Builder
	public static class RecommendPost {
		private Long id;
		private String postId;
		private String link;
		private String companyLogo;
		private String companyName;
		private String companySize;
		private long deadline;
		private boolean isSaved;
		private String title;
		private List<String> workPlaces;
		private List<String> experiences;
		private List<String> employments;
	}
}
