package navik.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.job.enums.JobType;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDTO {
    // 프로필사진, 레벨
    private Long boardId;
    private Long userId;
    private JobType jobType;
    private String nickname;
    private String articleTitle;
    private String articleContent;

    private int likeCount;
    private int commentCount;
    private int viewCount;

    private LocalDateTime createdAt;
}
