package navik.domain.portfolio.entity;

public enum PortfolioType {
	PDF_PENDING,      // PDF 업로드됨, 분석 전
	PDF_ANALYZED,     // PDF 업로드됨, 분석 완료
	TEXT_INPUT        // 텍스트 직접 입력
}
