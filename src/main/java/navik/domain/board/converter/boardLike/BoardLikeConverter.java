package navik.domain.board.converter.boardLike;

import navik.domain.board.dto.boardLike.BoardLikeDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.BoardLike;
import navik.domain.users.entity.User;

public class BoardLikeConverter {
	public static BoardLikeDTO.Parameter toParameter(Long userId, Long boardId) {
		return BoardLikeDTO.Parameter.builder()
			.userId(userId)
			.boardId(boardId)
			.build();
	}

	public static BoardLike toEntity(User user, Board board) {
		return BoardLike.builder()
			.user(user)
			.board(board)
			.build();
	}

	public static BoardLikeDTO.Response toResponse(Long boardId, Integer likeCount, Boolean isLiked) {
		return BoardLikeDTO.Response.builder()
			.boardId(boardId)
			.likeCount(likeCount)
			.isLiked(isLiked)
			.build();
	}
}
