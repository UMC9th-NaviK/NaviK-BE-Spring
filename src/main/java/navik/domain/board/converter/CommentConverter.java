package navik.domain.board.converter;

import navik.domain.board.dto.CommentCreateDTO;
import navik.domain.board.dto.CommentDeleteDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.users.entity.User;

public class CommentConverter {
	// 컨트롤러 입력을 서비스 파라미터로 변경
	public static CommentCreateDTO.Parameter toParameter(Long userId, Long boardId, CommentCreateDTO.Request request) {
		return CommentCreateDTO.Parameter.builder()
			.userId(userId)
			.boardId(boardId)
			.content(request.getContent())
			.build();
	}

	// 서비스
	public static Comment toComment(User user, Board board, String content) {
		return Comment.builder()
			.user(user)
			.board(board)
			.content(content)
			.build();
	}

	// 저장된 결과를 응답 DTO로 변환
	public static CommentCreateDTO.Response toResponse(Long commentId) {
		return CommentCreateDTO.Response.builder()
			.commentId(commentId)
			.build();
	}

	// 삭제 파라미터 생성
	public static CommentDeleteDTO.Parameter toDeleteParameter(Long userId, Long boardId, Long commentId) {
		return CommentDeleteDTO.Parameter.builder()
			.userId(userId)
			.boardId(boardId)
			.commentId(commentId)
			.build();
	}
}
