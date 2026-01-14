package navik.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class CommentListDTO {

    @Getter
    @Builder
    public static class Parameter { // 조회 요청 시 필요한 데이터
        private Long userId;
        private Long boardId;
        private Pageable pageable;
    }

    @Getter
    @Builder
    public static class Comment { // 응답으로 내려줄 댓글 데이터 형태
        private Long commentId;
        private Long userId;
        private Long parentCommentId;
        private String content;
        private String nickname;
        private Boolean isMyComment;
        private LocalDateTime createdAt;
        private List<Comment> childComments;
    }
}
