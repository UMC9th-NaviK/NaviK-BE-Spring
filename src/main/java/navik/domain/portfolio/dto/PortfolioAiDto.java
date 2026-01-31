package navik.domain.portfolio.dto;

public class PortfolioAiDto {

	public record OcrRequest(
		String fileUrl
	) {
	}

	public record OcrResponse(
		String content
	) {
	}
}
