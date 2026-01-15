package navik.domain.ability.entity;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Vector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "ability_embeddings")
public class AbilityEmbedding extends BaseEntity {

	@Id
	private Long id;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Array(length = 1536)
	@Column(name = "embedding", nullable = false)
	private Vector embedding;

	@OneToOne
	@JoinColumn(name = "ability_id")
	private Ability ability;
}
