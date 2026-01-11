package navik.domain.board.converter;

import navik.domain.board.dto.CommentCreateRequestDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;

public class CommentConverter {
    public static Comment toComment(CommentCreateRequestDTO request, Long userId, Board board, Comment parentComment) {
        return Comment.builder()
                .id(userId)
                .board(board)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();
    }
}
