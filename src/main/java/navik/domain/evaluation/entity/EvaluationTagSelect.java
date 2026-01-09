package navik.domain.evaluation.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.study.entity.StudyMember;
import navik.global.common.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "evaluationtagselect")
public class EvaluationTagSelect extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private EvaluationTag tag;
}
