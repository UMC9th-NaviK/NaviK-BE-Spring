package navik.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReplyDTO {

	@NoArgsConstructor
	@Getter
	public static class Request {
		@NotBlank(message = "댓글 내용을 적어주세요")
		private String content; // 대댓글 내용
	}

	@Builder
	@Getter
	public static class Parameter {
		private Long userId;
		private Long boardId;
		private Long commentId; // 부모 댓글 ID
		private String content; // 대댓글 내용
	}

	@Builder
	@Getter
	public static class Response {
		private Long commentId;
		private String profileImageUrl;
		private Integer level;
		private String nickname;
		private Boolean isEntryLevel;
	}
}
