package navik.domain.recruitment.repository.recruitment.projection;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import navik.domain.recruitment.entity.Recruitment;

@Getter
public class RecommendPostProjection {
	private final Recruitment recruitment;
	private final Double matchScore;

	@QueryProjection
	public RecommendPostProjection(Recruitment recruitment, Double matchScore) {
		this.recruitment = recruitment;
		this.matchScore = matchScore;
	}
}
