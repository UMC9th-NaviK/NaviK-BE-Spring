package navik.domain.recruitment.dto.position;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorRequest {
	private Double lastSimilarity;
	private Long lastMatchCount;
	private Long lastId;
}
