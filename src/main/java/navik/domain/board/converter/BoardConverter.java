package navik.domain.board.converter;

import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.entity.Board;

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
                .jobType(board.getUser().getJob().getJobType())
                .articleTitle(board.getArticleTitle())
                .articleContent(board.getArticleContent())
                .likeCount((int)likeCount)
                .commentCount((int)commentCount)
                .viewCount(board.getArticleViews())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
