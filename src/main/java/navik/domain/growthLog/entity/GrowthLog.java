package navik.domain.growthLog.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.kpi.enitiy.UserCard;
import navik.global.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "growth_logs")
public class GrowthLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_card__id", nullable = false)
    private UserCard userCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GrowthType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "score", nullable = false)
    private Integer score;

}
