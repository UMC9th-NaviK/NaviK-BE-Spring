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

	public record AnalyzeResponse(List<KpiScoreItem> scores) {
		public record KpiScoreItem(@JsonProperty("kpi_id") Long kpiId, @JsonProperty("kpi_name") String kpiName,
								   Integer score, String level) {
		}
	}
}
