package navik.domain.recruitment.converter.position;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import navik.domain.job.entity.Job;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

public class PositionConverter {

	public static PositionResponseDTO.RecommendedPosition toRecommendedPosition(
		User user,
		RecommendedPositionProjection recommendedPositionProjection,
		PositionRequestDTO.SearchCondition searchCondition
	) {

		// 1. 유저 자격 획득
		ExperienceType userExperience = user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED;
		EducationLevel userEducationLevel = user.getEducationLevel();
		List<MajorType> userMajors = user.getUserDepartments().stream()
			.map(major -> {
				try {
					return MajorType.valueOf(major.getDepartment().getName());
				} catch (IllegalArgumentException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();

		// 2. 포지션에 대한 지원 자격 확인
		Position position = recommendedPositionProjection.getPosition();
		boolean satisfyExperience = (position.getExperienceType() == null) || (position.getExperienceType().getOrder()
			<= userExperience.getOrder());
		boolean satisfyEducation = (position.getEducationLevel() == null) || (position.getEducationLevel().getOrder()
			<= userEducationLevel.getOrder());
		boolean satisfyMajor = (position.getMajorType() == null) || userMajors.contains(position.getMajorType());

		// 3. 해시태그 생성 (근무지/경력/고용형태 3가지는 기본)
		List<String> hashTags = new ArrayList<>();
		hashTags.add(position.getWorkPlace() == null ? "지역미기재" : position.getWorkPlace());
		hashTags.add(position.getExperienceType() == null ? "경력무관" : position.getExperienceType().getLabel());
		hashTags.add(position.getEmploymentType() == null ? "기타고용형태" : position.getEmploymentType().getLabel());
		if (searchCondition.getJobTypes() != null && !searchCondition.getJobTypes().isEmpty())
			hashTags.add(position.getJob().getName());
		if (searchCondition.getCompanySizes() != null && !searchCondition.getCompanySizes().isEmpty())
			hashTags.add(position.getRecruitment().getCompanySize().getLabel());
		if (searchCondition.getEducationLevels() != null && !searchCondition.getEducationLevels().isEmpty())
			hashTags.add(position.getEducationLevel().getLabel());
		if (searchCondition.getIndustryTypes() != null && !searchCondition.getIndustryTypes().isEmpty())
			hashTags.add(position.getRecruitment().getIndustryType() == null ?
				"기타산업"
				: position.getRecruitment().getIndustryType().getLabel());

		// 4. DTO 반환
		return PositionResponseDTO.RecommendedPosition.builder()
			.id(position.getId())
			.postId(position.getRecruitment().getPostId())
			.link(position.getRecruitment().getLink())
			.companyLogo(position.getRecruitment().getCompanyLogo())
			.companySize(
				position.getRecruitment().getCompanySize() == null ?
					"미분류"
					: position.getRecruitment().getCompanySize().getLabel()
			)
			.companyName(position.getRecruitment().getCompanyName())
			.endDate(position.getRecruitment().getEndDate())
			.dDay(
				position.getRecruitment().getEndDate() != null ?
					ChronoUnit.DAYS.between(
						LocalDate.now(),
						position.getRecruitment().getEndDate().toLocalDate()
					)
					: null
			)
			.title(position.getRecruitment().getTitle())
			.positionName(position.getName())
			.kpis(position.getPositionKpis().stream().map(PositionKpi::getContent).toList())
			.hashTags(hashTags)
			.satisfyExperience(satisfyExperience)
			.satisfyEducation(satisfyEducation)
			.satisfyMajor(satisfyMajor)
			.build();
	}

	public static Position toEntity(
		RecruitmentRequestDTO.Recruitment.Position position,
		Recruitment recruitment,
		Job job
	) {
		return Position.builder()
			.job(job)
			.recruitment(recruitment)
			.name(position.getName())
			.employmentType(position.getEmploymentType())
			.experienceType(position.getExperienceType())
			.educationLevel(position.getEducationLevel())
			.areaType(position.getAreaType())
			.majorType(position.getMajorType())
			.workPlace(position.getDetailAddress())
			.startDate(recruitment.getStartDate())
			.endDate(recruitment.getEndDate())
			.build();
	}
}
