package navik.domain.board.converter;

import navik.domain.board.dto.ReplyDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.users.entity.User;

public class ReplyConverter {

	public static ReplyDTO.Parameter toParameter(Long userId, Long boardId, Long commentId, ReplyDTO.Request request) {
		return ReplyDTO.Parameter.builder()
			.userId(userId)
			.boardId(boardId)
			.commentId(commentId)
			.content(request.getContent())
			.build();
	}

	public static Comment toComment(User user, Board board, Comment comment, String content) {
		return Comment.builder()
			.user(user)
			.board(board)
			.parentComment(comment)
			.content(content)
			.build();
	}

	public static ReplyDTO.Response toResponse(Long commentId) {
		return ReplyDTO.Response.builder()
			.commentId(commentId)
			.build();
	}
}
