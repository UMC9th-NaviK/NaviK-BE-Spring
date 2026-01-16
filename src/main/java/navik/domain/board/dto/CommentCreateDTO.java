package navik.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentCreateDTO {

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
		@NotBlank(message = "댓글 내용을 적어주세요")
		private String content;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Parameter {
		private Long userId; // 작성자 ID
		private Long boardId; // 게시글 ID
		private String content; // 댓글 내용
	}

	@Getter
	@Builder
	public static class Response {
		private Long commentId;
	}

}
