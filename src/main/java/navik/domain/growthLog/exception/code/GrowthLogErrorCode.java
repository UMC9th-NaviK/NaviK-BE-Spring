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

	INVALID_GROWTH_LOG_TYPE(
		HttpStatus.BAD_REQUEST,
		"GROWTH_LOG_400_02",
		"재시도할 수 없는 성장 로그 타입입니다."
	),

	INVALID_GROWTH_LOG_STATUS(
		HttpStatus.BAD_REQUEST,
		"GROWTH_LOG_400_03",
		"재시도할 수 없는 성장 로그 상태입니다."
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
	),

	GROWTH_LOG_STATUS_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"GROWTH_LOG_404_03",
		"존재하지 않는 성장 로그 상태입니다."),

	GROWTH_LOG_RETRY_LIMIT_EXCEEDED(
		HttpStatus.TOO_MANY_REQUESTS,
		"GROWTH_LOG_429_01",
		"성장 로그 재시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."
	),

	AI_EVALUATION_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"GROWTH_LOG_500_01",
		"AI 평가에 실패했습니다."),

	INVALID_REQUEST(
		HttpStatus.BAD_REQUEST,
		"GROWTH_LOG_400_04",
		"성장 로그 요청 값이 올바르지 않습니다."),

	AI_SERVER_URL_NOT_CONFIGURED(
		HttpStatus.BAD_REQUEST,
		"GROWTH_LOG_400_05",
		"AI 서버 주소가 설정되지 않았습니다."
	),

	PROCESSING_TOKEN_MISMATCH(
		HttpStatus.CONFLICT,
		"GROWTH_LOG_409_01",
		"성장 로그 처리 토큰이 일치하지 않습니다."
	),

	GROWTH_LOG_NOT_PROCESSING(
		HttpStatus.CONFLICT,
		"GROWTH_LOG_409_02",
		"성장 로그가 처리 중 상태가 아닙니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}