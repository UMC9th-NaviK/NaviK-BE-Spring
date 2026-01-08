package navik.domain.board.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.study.entity.StudyMember;
import navik.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "article_title", nullable = false)
    private String articleTitle;

    @Column(name = "article_content", nullable = false)
    private String articleContent;

    @Column(name = "article_views", nullable = false)
    private Integer articleViews;

    @Column(name = "article_deleted", nullable = false)
    private Boolean articleDeleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
