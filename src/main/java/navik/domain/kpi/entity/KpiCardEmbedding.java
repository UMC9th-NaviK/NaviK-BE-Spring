package navik.domain.kpi.entity;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Vector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "kpi_cards")
public class KpiCardEmbedding extends BaseEntity {

	@Id
	private Long id;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Array(length = 1536)
	@Column(name = "embedding", nullable = false)
	private Vector embedding;

	@OneToOne
	@MapsId
	@JoinColumn(name = "kpi_card_id")
	private KpiCard kpiCard;
}
