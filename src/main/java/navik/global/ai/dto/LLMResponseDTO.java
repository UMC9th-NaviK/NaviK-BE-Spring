package navik.global.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import navik.domain.job.enums.JobType;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EducationType;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;

/**
 * Structured Output 관리
 */
public class LLMResponseDTO {

	@ToString
	@Getter
	@NoArgsConstructor
	public static class Recruitment {
		private String link;
		private String title;
		private String postId;
		private String companyName;
		private String companyLogo;
		private CompanySize companySize;
		private IndustryType industryType;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private List<Position> positions;

		@ToString
		@Getter
		@NoArgsConstructor
		public static class Position {
			private String name;
			private JobType jobType;
			private EmploymentType employmentType;
			private ExperienceType experienceType;
			private EducationType educationType;
			private AreaType areaType;
			private String detailAddress;
			private List<String> kpis;
		}
	}
}
