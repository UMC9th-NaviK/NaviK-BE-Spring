package navik.domain.evaluation.entity;

import jakarta.persistence.*;
import lombok.*;
import navik.domain.evaluation.enums.TagType;
import navik.global.common.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "evaluationtag")
public class EvaluationTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TagType tagType; // CSV 파일 사용해서 매핑 예정

    @Column(name = "tag_content", nullable = false)
    private String tagContent;
}
