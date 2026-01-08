package navik.domain.study.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.auth.entity.Member;
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.enums.StudyRole;
import navik.global.common.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "study_member")
public class StudyMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyRole role;

    @Column(name = "attend", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendStatus attend;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "member_study_start", nullable = false)
    private LocalDateTime MemberStart;

    @Column(name = "study_end", nullable = false)
    private LocalDateTime MemberEnd;
}
