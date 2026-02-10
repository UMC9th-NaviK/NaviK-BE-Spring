package navik.domain.recruitment.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.enums.NotificationType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.IndustryType;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@BatchSize(size = 100)
@Table(name = "recruitments")
public class Recruitment extends BaseEntity implements Notifiable {

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

	@Column(name = "summary")
	private String summary;

	@Column(name = "company_size")
	@Enumerated(EnumType.STRING)
	private CompanySize companySize;

	@Column(name = "industry_type")
	@Enumerated(EnumType.STRING)
	private IndustryType industryType;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@BatchSize(size = 100)
	@OneToMany(mappedBy = "recruitment")
	private List<Position> positions;

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.RECRUITMENT;
	}

	@Override
	public Long getNotifiableId() {
		return this.id;
	}

	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public String getAdditionalInfo() {
		return this.link;
	}
}