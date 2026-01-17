package navik.domain.recruitment.converter.recruitment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.Recruitment;

public class RecruitmentConverter {

	public static RecruitmentResponseDTO.RecommendPost toRecommendPost(Recruitment recruitment) {

		long deadline = ChronoUnit.DAYS.between(LocalDateTime.now(), recruitment.getEndDate());

		List<String> workPlaces = recruitment.getPositions().stream()
			.map(Position::getWorkPlace)
			.toList();

		List<String> experiences = recruitment.getPositions().stream()
			.map(position -> position.getExperienceType() == null ? "경력무관" : position.getExperienceType().getLabel())
			.toList();

		List<String> employments = recruitment.getPositions().stream()
			.map(position -> position.getEmploymentType() == null ? "기타" : position.getEmploymentType().getLabel())
			.toList();

		return RecruitmentResponseDTO.RecommendPost.builder()
			.id(recruitment.getId())
			.postId(recruitment.getPostId())
			.link(recruitment.getLink())
			.companyLogo(recruitment.getCompanyLogo())
			.companyName(recruitment.getCompanyName())
			.companySize(recruitment.getCompanySize().getLabel())
			.deadline(deadline)
			.title(recruitment.getTitle())
			.workPlaces(workPlaces)
			.experiences(experiences)
			.employments(employments)
			.build();
	}
}
