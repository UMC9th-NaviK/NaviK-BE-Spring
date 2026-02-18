package navik.domain.kpi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.job.entity.Job;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "kpi_cards")
public class KpiCard extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "strong_title", nullable = false, length = 255)
	private String strongTitle;

	@Column(name = "strong_content", nullable = false, length = 2000)
	private String strongContent;

	@Column(name = "weak_title", nullable = false, length = 255)
	private String weakTitle;

	@Column(name = "weak_content", nullable = false, length = 2000)
	private String weakContent;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "shared_image_url", length = 500)
	private String sharedImageUrl;

	@OneToOne(mappedBy = "kpiCard")
	private KpiCardEmbedding kpiCardEmbedding;
}
