package navik.domain.recruitment.repository;

import java.util.List;

import navik.domain.job.entity.Job;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

public interface RecruitmentCustomRepository {
	List<Recruitment> findRecommendedPosts(
		User user,
		Job job,
		EducationLevel EducationLevel,
		ExperienceType experienceType,
		List<MajorType> majorTypes
	);

	List<Recruitment> findRecommendedPostsByCard(
		KpiCard kpiCard,
		Job job
	);
}
