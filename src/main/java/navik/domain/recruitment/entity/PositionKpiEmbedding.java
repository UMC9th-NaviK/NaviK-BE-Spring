package navik.domain.recruitment.entity;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "position_kpi_embeddings")
public class PositionKpiEmbedding extends BaseEntity {

	@Id
	private Long id;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Array(length = 1536)
	@Column(name = "embedding", nullable = false)
	private float[] embedding;

	@OneToOne
	@MapsId
	@JoinColumn(name = "position_kpi_id")
	private PositionKpi positionKpi;
}
