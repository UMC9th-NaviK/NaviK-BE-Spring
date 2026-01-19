package navik.domain.portfolio.entity;

public enum PortfolioStatus {
	PDF_PENDING,      // PDF 업로드됨, 분석 전
	PDF_ANALYZED,     // PDF 업로드됨, 분석 완료
	TEXT_INPUT;        // 텍스트 직접 입력

	public static PortfolioStatus from(InputType inputType) {
		return switch (inputType) {
			case PDF -> PDF_PENDING;
			case TEXT -> TEXT_INPUT;
		};
	}
}

