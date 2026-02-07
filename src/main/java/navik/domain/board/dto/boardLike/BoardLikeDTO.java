package navik.domain.board.dto.boardLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BoardLikeDTO {
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Parameter {
		private Long userId;
		private Long boardId;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Response {
		private Long boardId;
		private Integer likeCount; // 총 좋아요 수
		private Boolean isLiked; // 현재 좋아요 상태
	}
}
