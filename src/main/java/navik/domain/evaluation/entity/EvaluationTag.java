package navik.domain.evaluation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.evaluation.enums.Tag;
import navik.domain.evaluation.enums.TagType;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "evaluation_tags")
public class EvaluationTag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tag", nullable = false)
	@Enumerated(EnumType.STRING)
	private Tag tag; // 협업, 커뮤니케이션 등등

	@Column(name = "tag_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TagType tagType; // 강점, 약점

	@Column(name = "category_name", nullable = false)
	private String categoryName;

	@Column(name = "tag_content", nullable = false)
	private String tagContent;
}
