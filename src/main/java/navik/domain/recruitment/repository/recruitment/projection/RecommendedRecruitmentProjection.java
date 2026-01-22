package navik.domain.recruitment.repository.recruitment.projection;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import navik.domain.recruitment.entity.Recruitment;

@Getter
public class RecommendedRecruitmentProjection {
	private final Recruitment recruitment;
	private final Double matchScore;
	private final Long matchCount;

	@QueryProjection
	public RecommendedRecruitmentProjection(Recruitment recruitment, Double matchScore, Long matchCount) {
		this.recruitment = recruitment;
		this.matchScore = matchScore;
		this.matchCount = matchCount;
	}
}
