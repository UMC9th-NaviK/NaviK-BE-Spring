package navik.domain.kpi.enitiy;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.users.entity.User;
import navik.global.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_card",
    uniqueConstraints = @UniqueConstraint(columnNames = {"kpi_card_id", "user_id"}))
public class UserCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_card_id")
    private KpiCard kpiCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_score", nullable = false)
    private Long totalScore;
}
