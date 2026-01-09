package navik.domain.evaluation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.study.entity.Study;
import navik.domain.users.entity.User;
import navik.global.common.BaseEntity;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "evaluations")
public class Evaluation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	private Study study;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluator_id")
	private User evaluator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluatee_id")
	private User evaluatee;

	@Column(name = "score", nullable = false)
	private float score;

	@Column(name = "comment", nullable = false)
	private String comment;
}
