package navik.domain.kpi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.kpi.enums.KpiCardQuestionType;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
	name = "kpi_card_questions",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_kpi_card_type",
		columnNames = {"kpi_card_id", "type"}
	)
)
public class KpiCardQuestion extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", nullable = false)
	private String content;

	@ManyToOne
	@JoinColumn(name = "kpi_card_id", nullable = false)
	private KpiCard kpiCard;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private KpiCardQuestionType type;
}
