package navik.domain.board.converter;

import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.CommentCreateRequestDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.users.entity.User;

public class BoardConverter {

    public static BoardResponseDTO toResponse(
            Board board,
            long likeCount,
            long commentCount
    ) {
        return BoardResponseDTO.builder()
                .boardId(board.getId())
                .userId(board.getUser().getId())
                .nickname(board.getUser().getNickname())
                .articleTitle(board.getArticleTitle())
                .articleContent(board.getArticleContent())
                .likeCount((int)likeCount)
                .commentCount((int)commentCount)
                .viewCount(board.getArticleViews())
                .createdAt(board.getCreatedAt())
                .build();
    }

    public static Comment toComment(CommentCreateRequestDTO request, Long userId, Board board, Comment parentComment) {
        return Comment.builder()
                .id(userId)
                .board(board)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();
    }
}
