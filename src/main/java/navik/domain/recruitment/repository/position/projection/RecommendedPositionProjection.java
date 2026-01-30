package navik.domain.recruitment.repository.position.projection;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import navik.domain.recruitment.entity.Position;

@Getter
public class RecommendedPositionProjection {
	private final Position position;
	private final Double matchScore;
	private final Long matchCount;

	@QueryProjection
	public RecommendedPositionProjection(Position position, Double matchScore, Long matchCount) {
		this.position = position;
		this.matchScore = matchScore;
		this.matchCount = matchCount;
	}
}
