package navik.domain.recruitment.repository;

import java.util.List;

import navik.domain.job.entity.Job;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.EducationType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.users.entity.User;

public interface RecruitmentCustomRepository {
	List<Recruitment> findRecommendedPosts(
		User user,
		Job job,
		EducationType educationType,
		ExperienceType experienceType,
		MajorType majorType
	);
}
