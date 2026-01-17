package navik.domain.recruitment.converter.recruitment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.repository.recruitment.projection.RecommendPostProjection;

public class RecruitmentConverter {

	public static RecruitmentResponseDTO.RecommendedPost toRecommendedPost(
		RecommendPostProjection recommendPostProjection) {

		Recruitment recruitment = recommendPostProjection.getRecruitment();
		Double matchScore = recommendPostProjection.getMatchScore();

		List<Position> positions = recruitment.getPositions();
		Position position = positions.getFirst();

		return RecruitmentResponseDTO.RecommendedPost.builder()
			.id(recruitment.getId())
			.postId(recruitment.getPostId())
			.link(recruitment.getLink())
			.companyLogo(recruitment.getCompanyLogo())
			.companyName(recruitment.getCompanyName())
			.companySize(recruitment.getCompanySize().getLabel())
			.deadline(ChronoUnit.DAYS.between(LocalDateTime.now(), recruitment.getEndDate()))
			.title(recruitment.getTitle())
			.workPlace(
				positions.size() > 1
					? String.format("%s 외 %d", position.getWorkPlace(), positions.size() - 1)
					: position.getWorkPlace()
			)
			.experience(
				Optional.ofNullable(position.getExperienceType())
					.map(ExperienceType::getLabel)
					.orElse("경력무관")
			)
			.employment(
				Optional.ofNullable(position.getEmploymentType())
					.map(EmploymentType::getLabel)
					.orElse("기타")
			)
			.isRecommend(matchScore >= 1.0)
			.aiSummary(recruitment.getSummary())
			.build();
	}
}
