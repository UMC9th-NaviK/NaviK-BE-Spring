package navik.domain.kpi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum KpiCardErrorCode implements BaseCode {

	KPI_CARD_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"KPI_CARD_404_01",
		"존재하지 않는 KPI 카드입니다."
	),

	INVALID_KPI_CARD_TYPE(
		HttpStatus.BAD_REQUEST,
		"KPI_CARD_400_01",
		"유효하지 않은 KPI 카드 타입입니다."
	),

	KPI_CARD_NOT_INITIALIZED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"KPI_CARD_500_01",
		"해당 직무에 KPI 카드가 등록되지 않았습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
