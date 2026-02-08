package navik.domain.portfolio.entity;

public enum PortfolioStatus {
	PENDING,    // 평가 대기
	PROCESSING, // 평가 처리 중
	COMPLETED,  // 평가 완료
	RETRY_REQUIRED, // 재평가 필요
	FAILED      // 평가 실패

}

