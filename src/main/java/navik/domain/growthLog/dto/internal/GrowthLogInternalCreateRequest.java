package navik.domain.growthLog.dto.internal;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record GrowthLogInternalCreateRequest(

	@NotEmpty(message = "KPI 변경 목록은 비어 있을 수 없습니다.")
	List<@Valid KpiDelta> kpis,

	String content
) {

	public record KpiDelta(

		@NotNull(message = "kpiCardId는 필수입니다.")
		Long kpiCardId,

		@NotNull(message = "delta는 필수입니다.")
		Integer delta
	) {
	}
}