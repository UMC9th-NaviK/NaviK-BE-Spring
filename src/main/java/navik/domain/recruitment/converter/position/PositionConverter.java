package navik.domain.recruitment.converter.position;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import navik.domain.job.entity.Job;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.position.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

public class PositionConverter {

	public static PositionResponseDTO.RecommendedPosition toRecommendedPosition(
		User user,
		RecommendedPositionProjection projection,
		Map<Long, List<String>> kpiMap,
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

		// 2. 포지션에 대한 지원 자격 확인 (경력, 학력, 전공)
		boolean satisfyExperience = (projection.getExperienceType() == null) ||
			(projection.getExperienceType().getOrder() <= userExperience.getOrder());
		boolean satisfyEducation = (projection.getEducationLevel() == null) ||
			(userEducationLevel != null
				&& projection.getEducationLevel().getOrder() <= userEducationLevel.getOrder());
		boolean satisfyMajor =
			(projection.getMajorType() == null) || userMajors.contains(projection.getMajorType());

		// 3. 해시태그 생성 (근무지/경력/고용형태 3가지는 기본, 나머지는 필터 선택 시 등장)
		List<String> hashTags = new ArrayList<>();
		hashTags.add(projection.getWorkPlace() == null ? "지역미기재" : projection.getWorkPlace());
		hashTags.add(projection.getExperienceType() == null ? "경력무관" : projection.getExperienceType().getLabel());
		hashTags.add(projection.getEmploymentType() == null ? "기타고용형태" : projection.getEmploymentType().getLabel());
		if (searchCondition.getJobTypes() != null && !searchCondition.getJobTypes().isEmpty())
			hashTags.add(projection.getJobName() == null ? "직무미기재" : projection.getJobName());
		if (searchCondition.getCompanySizes() != null && !searchCondition.getCompanySizes().isEmpty())
			hashTags.add(projection.getCompanySize() == null ? "규모미기재" : projection.getCompanySize().getLabel());
		if (searchCondition.getEducationLevels() != null && !searchCondition.getEducationLevels().isEmpty())
			hashTags.add(projection.getEducationLevel() == null ? "학력미기재" : projection.getEducationLevel().getLabel());
		if (searchCondition.getIndustryTypes() != null && !searchCondition.getIndustryTypes().isEmpty())
			hashTags.add(projection.getIndustryType() == null ? "기타산업" : projection.getIndustryType().getLabel());

		// 4. DTO 반환
		return PositionResponseDTO.RecommendedPosition.builder()
			.id(projection.getId())
			.postId(projection.getPostId())
			.link(projection.getLink())
			.companyLogo(projection.getCompanyLogo())
			.companySize(projection.getCompanySize() == null ? "미분류" : projection.getCompanySize().getLabel())
			.companyName(projection.getCompanyName())
			.endDate(projection.getEndDate())
			.dDay(
				projection.getEndDate() != null ?
					ChronoUnit.DAYS.between(LocalDate.now(), projection.getEndDate().toLocalDate()) :
					null)
			.title(projection.getTitle())
			.positionName(projection.getName())
			.kpis(kpiMap.getOrDefault(projection.getId(), List.of()))
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
			.build();
	}
}
