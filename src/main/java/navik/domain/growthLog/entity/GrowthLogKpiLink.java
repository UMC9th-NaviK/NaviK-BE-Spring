package navik.domain.growthLog.entity;

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
import navik.domain.kpi.entity.KpiCard;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "growth_log_kpi_links")
public class GrowthLogKpiLink extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "growth_log_id", nullable = false)
	private GrowthLog growthLog;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "kpi_card_id", nullable = false)
	private KpiCard kpiCard;

	@Column(nullable = false)
	private Integer delta;

	void attachGrowthLog(GrowthLog growthLog) {
		this.growthLog = growthLog;
	}
}
