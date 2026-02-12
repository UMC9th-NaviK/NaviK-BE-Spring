package navik.domain.recruitment.repository.position.position.projection;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.users.enums.EducationLevel;

@Getter
public class RecommendedPositionProjection {
	// Position
	private Long id;
	private String name;
	private ExperienceType experienceType;
	private EducationLevel educationLevel;
	private MajorType majorType;
	private String workPlace;
	private EmploymentType employmentType;

	// Recruitment
	private String postId;
	private String link;
	private String companyLogo;
	private CompanySize companySize;
	private String companyName;
	private LocalDateTime endDate;
	private IndustryType industryType;
	private String title;

	// Job
	private String jobName;

	// Cursor
	private Double similarityAvg;
	private Long matchCount;

	@QueryProjection
	public RecommendedPositionProjection(
		Long id, String name, ExperienceType experienceType, EducationLevel educationLevel, MajorType majorType,
		String workPlace, EmploymentType employmentType, // Position
		String postId, String link, String companyLogo, CompanySize companySize, String companyName,
		LocalDateTime endDate, IndustryType industryType, String title, // Recruitment
		String jobName, // Job
		Double similarityAvg, Long matchCount // Cursor
	) {
		this.id = id;
		this.name = name;
		this.experienceType = experienceType;
		this.educationLevel = educationLevel;
		this.majorType = majorType;
		this.workPlace = workPlace;
		this.employmentType = employmentType;
		this.postId = postId;
		this.link = link;
		this.companyLogo = companyLogo;
		this.companySize = companySize;
		this.companyName = companyName;
		this.endDate = endDate;
		this.industryType = industryType;
		this.title = title;
		this.jobName = jobName;
		this.similarityAvg = similarityAvg;
		this.matchCount = matchCount;
	}
}
