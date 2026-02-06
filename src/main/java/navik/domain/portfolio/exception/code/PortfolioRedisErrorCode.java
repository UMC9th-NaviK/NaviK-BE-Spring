package navik.domain.portfolio.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@RequiredArgsConstructor
public enum PortfolioRedisErrorCode implements BaseCode {

	STREAM_PUBLISH_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"PORTFOLIO_REDIS_500_01",
		"포트폴리오 분석 메시지 발행에 실패했습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
