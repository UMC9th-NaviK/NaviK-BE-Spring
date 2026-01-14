package navik.domain.kpi.repository.projection;

public interface KpiCardPercentileView {

    Integer getScore();

    Integer getTopPercent();

    Integer getBottomPercent();

}
