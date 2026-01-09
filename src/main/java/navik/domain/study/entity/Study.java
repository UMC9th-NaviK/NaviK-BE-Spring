package navik.domain.study.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.study.enums.RecruitmentStatus;
import navik.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "studies")
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_title", nullable = false)
    private String studyTitle;

    @Column(name = "study_limit", nullable = false)
    private Integer studyLimit;

    @Column(name = "study_description", nullable = false)
    private String studyDescription;

    @Column(name = "study_start", nullable = false)
    private LocalDateTime studyStart;

    @Column(name = "study_end", nullable = false)
    private LocalDateTime studyEnd;

    @Column(name = "recruitment", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitment;

    @Column(name = "social_id", nullable = false)
    private String socialId;
}
