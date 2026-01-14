package navik.domain.board.dto;

import lombok.Builder;
import lombok.Getter;

public class CommentDeleteDTO {

    @Builder
    @Getter
    public static class Parameter {
        private Long userId;
        private Long boardId;
        private Long commentId;
    }
}
