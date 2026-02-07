package navik.domain.board.dto.comment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;

import lombok.Builder;
import lombok.Getter;

public class CommentListDTO {

	@Getter
	@Builder
	public static class Parameter { // 조회 요청 시 필요한 데이터
		private Long userId;
		private Long boardId;
		private Pageable pageable;
	}

	@Getter
	@Builder
	public static class ResponseComment { // 응답으로 내려줄 댓글 데이터 형태
		private Long boardId;
		private Long commentId;
		private Long userId;
		private Long parentCommentId;
		private String content;
		private String nickname;
		private String profileImageUrl;
		private Integer level;
		private Boolean isMyComment;
		private Boolean isEntryLevel;
		private String jobName;
		private LocalDateTime createdAt;
		private List<ResponseComment> childResponseComments;
	}
}
