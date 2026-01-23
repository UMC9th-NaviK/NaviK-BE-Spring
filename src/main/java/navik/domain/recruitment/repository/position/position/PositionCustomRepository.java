package navik.domain.recruitment.repository.position.position;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import navik.domain.job.entity.Job;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.repository.position.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;

public interface PositionCustomRepository {
	Slice<RecommendedPositionProjection> findRecommendedPositions(
		User user,
		List<Job> jobs,
		PositionRequestDTO.SearchCondition searchCondition,
		PositionRequestDTO.CursorRequest cursorRequest,
		Pageable pageable
	);

	void batchSaveAll(List<Position> positions);
}
