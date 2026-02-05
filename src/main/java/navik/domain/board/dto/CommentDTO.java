package navik.domain.board.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
	private Long commentId;
	private Long userId;
	private Long parentCommentId;
	private String profileImageUrl;
	private Integer level;
	private String nickname;
	private String commentContent;
	private LocalDateTime createdAt;
}
