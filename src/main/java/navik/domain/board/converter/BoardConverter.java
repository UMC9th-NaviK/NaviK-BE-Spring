package navik.domain.board.converter;

import navik.domain.board.dto.BoardDTO;
import navik.domain.board.entity.Board;

public class BoardConverter {

    public static BoardDTO toResponse(
            Board board,
            long likeCount,
            long commentCount
    ) {
        return BoardDTO.builder()
                .boardId(board.getId())
                .userId(board.getUser().getId())
                .jobType(board.getUser().getJob().getName()) // 경력도 추가해야함
                .nickname(board.getUser().getNickname())
                .articleTitle(board.getArticleTitle())
                .articleContent(board.getArticleContent())
                .likeCount((int)likeCount)
                .commentCount((int)commentCount)
                .viewCount(board.getArticleViews())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
