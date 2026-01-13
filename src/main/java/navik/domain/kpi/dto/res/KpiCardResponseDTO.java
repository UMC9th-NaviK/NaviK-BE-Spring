package navik.domain.kpi.dto.res;

public class KpiCardResponseDTO {

    public record GridItem(
            Long kpiCardId,
            String name
    ) {
    }
}
