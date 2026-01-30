package navik.domain.recruitment.repository.recruitment;

import java.util.List;

import navik.domain.job.entity.Job;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.recruitment.projection.RecommendedRecruitmentProjection;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

public interface RecruitmentCustomRepository {
	List<RecommendedRecruitmentProjection> findRecommendedPosts(
		User user,
		Job job,
		EducationLevel EducationLevel,
		ExperienceType experienceType,
		List<MajorType> majorTypes
	);

	List<RecommendedRecruitmentProjection> findRecommendedPostsByCard(
		KpiCard kpiCard,
		Job job
	);
}
