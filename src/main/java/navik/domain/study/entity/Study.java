package navik.domain.study.entity;

import java.time.LocalDateTime;

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
import navik.domain.study.enums.RecruitmentStatus;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
