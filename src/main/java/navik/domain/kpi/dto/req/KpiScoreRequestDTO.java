package navik.domain.kpi.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class KpiScoreRequestDTO {

    public record Initialize(

            @NotEmpty(message = "KPI 점수 목록은 비어 있을 수 없습니다.")
            @Valid
            List<Item> scores

    ) {
    }

    public record Item(

            @NotNull(message = "kpiCardId는 필수입니다.")
            Long kpiCardId,

            @NotNull(message = "score는 필수입니다.")
            @Min(value = 0, message = "score는 0 이상이어야 합니다.")
            @Max(value = 100, message = "score는 100 이하여야 합니다.")
            Integer score
    ) {
    }
}
