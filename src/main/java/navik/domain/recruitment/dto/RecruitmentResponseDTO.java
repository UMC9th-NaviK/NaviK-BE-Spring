package navik.domain.recruitment.dto;

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

public class RecruitmentResponseDTO {

	@ToString
	@Getter
	@NoArgsConstructor
	public static class LLMResponse {
		private String link;
		private String title;
		private String postId;
		private String companyName;
		private String companyLogo;
		private CompanySize companySize;
		private IndustryType industryType;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private List<PositionDTO> positions;
	}

	@ToString
	@Getter
	@NoArgsConstructor
	public static class PositionDTO {
		private String name;
		private JobType jobType;
		private EmploymentType employmentType;
		private ExperienceType experienceType;
		private EducationType educationType;
		private AreaType areaType;
		private List<String> kpis;
	}
}
