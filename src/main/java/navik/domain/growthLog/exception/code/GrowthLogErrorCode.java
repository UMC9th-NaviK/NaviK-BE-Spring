package navik.domain.growthLog.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@RequiredArgsConstructor
public enum GrowthLogErrorCode implements BaseCode {

	GROWTH_LOG_NOT_FOUND_OR_ALREADY_MAPPED(
		HttpStatus.BAD_REQUEST,
		"GROWTH_LOG_400_01",
		"성장 로그가 존재하지 않거나 이미 KPI가 매핑되었습니다."
	),

	KPI_CARD_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"GROWTH_LOG_404_01",
		"존재하지 않는 KPI 카드입니다."
	),

	GROWTH_LOG_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"GROWTH_LOG_404_02",
		"존재하지 않는 성장 로그입니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}