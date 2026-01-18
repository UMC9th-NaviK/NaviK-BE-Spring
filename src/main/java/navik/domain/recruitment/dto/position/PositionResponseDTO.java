package navik.domain.recruitment.dto.position;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

public class PositionResponseDTO {

	@Getter
	@Builder
	public static class RecommendedPosition {
		private Long id;
		private String postId;
		private String link;
		private String companyLogo;
		private String companyName;
		private String companySize;
		private LocalDateTime endDate;
		private long dDay;
		private String title;
		private String positionName;
		private List<String> kpis;
		private List<String> hashTags;
		private boolean satisfyExperience;
		private boolean satisfyEducation;
		private boolean satisfyMajor;
	}
}