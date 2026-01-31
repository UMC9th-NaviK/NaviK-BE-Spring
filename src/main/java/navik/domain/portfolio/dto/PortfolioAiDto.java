package navik.domain.portfolio.dto;

public class PortfolioAiDto {

	public record OcrRequest(
		String pdfUrl
	) {
	}

	public record OcrResponse(
		String text
	) {
	}
}
