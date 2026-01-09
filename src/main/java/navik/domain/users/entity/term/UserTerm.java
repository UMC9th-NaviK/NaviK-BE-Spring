package navik.domain.users.entity.term;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.users.entity.User;
import navik.global.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_terms",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "term_id"}))
public class UserTerm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(name = "is_agreed", nullable = false)
	@Builder.Default
    private Boolean isAgreed = false;
}
