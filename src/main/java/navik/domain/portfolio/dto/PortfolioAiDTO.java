package navik.domain.portfolio.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortfolioAiDTO {

	public record OcrRequest(String pdfUrl) {
	}

	public record OcrResponse(String text) {
	}

	public record AnalyzeRequest(@JsonProperty("resume_text") String resumeText) {
	}

	public record FallbackRequest(@JsonProperty("q_b1") Integer qB1, @JsonProperty("q_b2") Integer qB2,
								  @JsonProperty("q_b3") Integer qB3, @JsonProperty("q_b4") Integer qB4,
								  @JsonProperty("q_b5") Integer qB5) {
	}

	public record AnalyzeResponse(List<KpiScoreItem> scores, List<Abilities> abilities) {
		public record KpiScoreItem(@JsonProperty("kpi_id") Long kpiId, @JsonProperty("kpi_name") String kpiName,
								   Integer score, String basis) {
		}
	}

	public record Abilities(String content, float[] embedding) {
	}
}
