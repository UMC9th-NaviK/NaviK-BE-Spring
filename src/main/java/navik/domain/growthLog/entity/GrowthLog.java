package navik.domain.growthLog.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.users.entity.User;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
	name = "growth_logs",
	indexes = {
		@Index(
			name = "idx_growthlog_user_createdat",
			columnList = "user_id, created_at"
		),
		@Index(
			name = "idx_growthlog_user_type_createdat",
			columnList = "user_id, type, created_at"
		)
	}
)
public class GrowthLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private GrowthType type;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(nullable = false)
	@Builder.Default
	private Integer totalDelta = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private GrowthLogStatus status = GrowthLogStatus.COMPLETED;

	@OneToMany(mappedBy = "growthLog", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<GrowthLogKpiLink> kpiLinks = new ArrayList<>();

	public void addKpiLink(GrowthLogKpiLink link) {
		kpiLinks.add(link);
		link.attachGrowthLog(this);
	}

}
