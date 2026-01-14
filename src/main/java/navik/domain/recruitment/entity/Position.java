package navik.domain.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import navik.domain.job.entity.Job;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.EducationType;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "positions")
public class Position extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id")
	private Job job;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recruitment_id")
	private Recruitment recruitment;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "employment_type")
	@Enumerated(EnumType.STRING)
	private EmploymentType employmentType;

	@Column(name = "experience_type")
	@Enumerated(EnumType.STRING)
	private ExperienceType experienceType;

	@Column(name = "education_type")
	@Enumerated(EnumType.STRING)
	private EducationType educationType;

	@Column(name = "area_type")
	@Enumerated(EnumType.STRING)
	private AreaType areaType;

	@Column(name = "detail_address")
	private String detailAddress;
}
