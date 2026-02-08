package navik.domain.portfolio.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.exception.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum PortfolioErrorCode implements BaseCode {

	PORTFOLIO_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"PORTFOLIO_404_01",
		"포트폴리오를 찾을 수 없습니다."
	),
	PORTFOLIO_NOT_OWNED(
		HttpStatus.FORBIDDEN,
		"PORTFOLIO_403_01",
		"해당 포트폴리오에 접근 권한이 없습니다."
	),
	INVALID_PORTFOLIO_STATUS(
		HttpStatus.BAD_REQUEST,
		"PORTFOLIO_400_01",
		"재분석이 필요한 상태에서만 추가 정보를 입력할 수 있습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
