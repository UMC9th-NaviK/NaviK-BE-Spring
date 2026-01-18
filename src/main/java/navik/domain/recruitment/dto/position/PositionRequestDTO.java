package navik.domain.recruitment.dto.position;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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
		@Schema(description = "희망 직무 목록")
		private List<JobType> jobTypes;
		@Schema(description = "경력 요건 목록")
		private List<ExperienceType> experienceTypes;
		@Schema(description = "고용 형태 목록")
		private List<EmploymentType> employmentTypes;
		@Schema(description = "회사 규모 목록")
		private List<CompanySize> companySizes;
		@Schema(description = "학력 목록")
		private List<EducationLevel> educationLevels;
		@Schema(description = "근무 지역 목록")
		private List<AreaType> areaTypes;
		@Schema(description = "관심 산업 목록")
		private List<IndustryType> industryTypes;
		@Schema(description = "끝난 공고도 함께 보기")
		private boolean withEnded;
	}
}
