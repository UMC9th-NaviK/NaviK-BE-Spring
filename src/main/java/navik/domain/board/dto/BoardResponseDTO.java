package navik.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDTO {
    private Long boardId;
    private Long userId;
    private String nickname;
    private String articleTitle;
    private String articleContent;

    private int likeCount;
    private int commentCount;
    private int viewCount;

    private LocalDateTime createdAt;
}
