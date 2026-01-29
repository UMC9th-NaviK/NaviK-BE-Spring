package navik.domain.recruitment.dto.recruitment;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;
import navik.domain.recruitment.enums.JobType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.users.enums.EducationLevel;

public class RecruitmentRequestDTO {

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
		private String summary;

		@Getter
		@NoArgsConstructor
		public static class Position {
			private String name;
			private JobType jobType;
			private EmploymentType employmentType;
			private ExperienceType experienceType;
			private EducationLevel educationLevel;
			private AreaType areaType;
			private String detailAddress;
			private MajorType majorType;
			private List<KPI> kpis;

			@Getter
			@NoArgsConstructor
			public static class KPI {
				private String kpi;
				private float[] embedding;
			}
		}
	}
}
