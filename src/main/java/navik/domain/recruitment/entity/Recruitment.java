package navik.domain.recruitment.entity;

import java.time.LocalDate;

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
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.IndustryType;
import navik.global.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "recruitments")
public class Recruitment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "post_id", nullable = false, unique = true)
	private String postId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "link", nullable = false)
	private String link;

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "company_logo")
	private String companyLogo;

	@Column(name = "company_size")
	@Enumerated(EnumType.STRING)
	private CompanySize companySize;

	@Column(name = "industry_type")
	@Enumerated(EnumType.STRING)
	private IndustryType industryType;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;
}
