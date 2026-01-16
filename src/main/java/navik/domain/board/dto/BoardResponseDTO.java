package navik.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BoardResponseDTO {
	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BoardDTO {
		// 프로필사진, 레벨
		private Long boardId;
		private Long userId;
		private String jobType;
		private String nickname;
		private String articleTitle;
		private String articleContent;
		private int likeCount;
		private int commentCount;
		private int viewCount;
		private LocalDateTime createdAt;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class HotBoardListDTO {
		private List<BoardDTO> boardList;
		private String nextCursor;
		private Boolean hasNext;
	}
}

