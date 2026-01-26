package navik.domain.recruitment.repository.recruitment;

import java.util.List;

import org.springframework.data.domain.Pageable;

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
		EducationLevel educationLevel,
		ExperienceType experienceType,
		List<MajorType> majorTypes,
		Pageable pageable
	);

	List<RecommendedRecruitmentProjection> findRecommendedPostsByCard(
		KpiCard kpiCard,
		Job job
	);
}
