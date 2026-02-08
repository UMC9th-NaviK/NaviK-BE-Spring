package navik.domain.board.dto.board;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		@JsonDeserialize(using = LocalDateTimeDeserializer.class)
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

