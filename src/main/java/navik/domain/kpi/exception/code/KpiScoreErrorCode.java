package navik.domain.kpi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum KpiScoreErrorCode implements BaseCode {

	EMPTY_KPI_SCORES(
		HttpStatus.BAD_REQUEST,
		"KPI_SCORE_400_01",
		"KPI 점수 목록은 비어 있을 수 없습니다."
	),

	INVALID_KPI_SCORE_REQUEST(
		HttpStatus.BAD_REQUEST,
		"KPI_SCORE_400_02",
		"KPI 점수 요청 값이 올바르지 않습니다."
	),

	DUPLICATED_KPI_CARD_ID(
		HttpStatus.BAD_REQUEST,
		"KPI_SCORE_400_03",
		"중복된 KPI 카드 ID가 존재합니다."
	),

	SCORE_OUT_OF_RANGE(
		HttpStatus.BAD_REQUEST,
		"KPI_SCORE_400_04",
		"KPI 점수는 허용된 범위를 벗어났습니다."
	),

	KPI_SCORE_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"KPI_SCORE_404_01",
		"존재하지 않는 KPI 점수입니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
