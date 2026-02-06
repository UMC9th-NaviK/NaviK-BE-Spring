package navik.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

public class BoardResponseDTO {
	@Getter
	@Builder
	public static class BoardDTO {
		private Long boardId;
		private Long userId;
		private String jobName;
		private String nickname;
		private String profileImageUrl;
		private Integer level;
		private Boolean isEntryLevel;
		private String articleTitle;
		private String articleContent;
		private int likeCount;
		private int commentCount;
		private int viewCount;
		private LocalDateTime createdAt;
	}

	@Getter
	@Builder
	public static class HotBoardListDTO {
		private List<BoardDTO> boardList;
		private String nextCursor;
		private Boolean hasNext;
	}
}

