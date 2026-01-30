package navik.domain.recruitment.dto.position;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;
import navik.domain.recruitment.enums.JobType;
import navik.domain.users.enums.EducationLevel;

public class PositionRequestDTO {

	@Getter
	@NoArgsConstructor
	public static class SearchCondition {
		private List<JobType> jobTypes;
		private List<ExperienceType> experienceTypes;
		private List<EmploymentType> employmentTypes;
		private List<CompanySize> companySizes;
		private List<EducationLevel> educationLevels;
		private List<AreaType> areaTypes;
		private List<IndustryType> industryTypes;
		private boolean withEnded;
	}

	@Getter
	@Builder
	public static class CursorRequest {
		private Double lastSimilarity;
		private Long lastMatchCount;
		private Long lastId;
	}
}
