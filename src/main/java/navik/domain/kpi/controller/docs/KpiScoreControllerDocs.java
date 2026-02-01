package navik.domain.kpi.controller.docs;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.swagger.ApiErrorCodes;

@Tag(name = "KPI Score", description = "KPI 점수 API")
public interface KpiScoreControllerDocs {

	@Operation(
		summary = "KPI 점수 초기화(생성/업데이트)",
		description = """
			요청으로 받은 카드 점수를 기준으로 사용자 KPI 점수를 초기화합니다.
			- 이미 존재하면 update
			- 없으면 create
			"""
	)
	@ApiErrorCodes(
		enumClass = KpiScoreErrorCode.class,
		includes = {
			"EMPTY_KPI_SCORES",
			"INVALID_KPI_SCORE_REQUEST",
			"DUPLICATED_KPI_CARD_ID",
			"SCORE_OUT_OF_RANGE"
		}
	)
	@ApiErrorCodes(
		enumClass = KpiCardErrorCode.class,
		includes = {
			"KPI_CARD_NOT_FOUND",
			"KPI_CARD_NOT_INITIALIZED"
		}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "생성/초기화 성공"
		)
	})
	ApiResponse<KpiScoreResponseDTO.Initialize> initialize(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Valid @RequestBody KpiScoreRequestDTO.Initialize request
	);

	@Operation(
		summary = "KPI 점수 증감",
		description = """
			특정 카드(kpiCardId)의 점수를 증감합니다.
			- request 또는 request.delta가 null이면 delta=1로 처리합니다.
			"""
	)
	@ApiErrorCodes(
		enumClass = KpiScoreErrorCode.class,
		includes = {
			"KPI_SCORE_NOT_FOUND",
			"SCORE_OUT_OF_RANGE"
		}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "증감 성공"
		)
	})
	ApiResponse<KpiScoreResponseDTO.Increment> increment(
		@Parameter(hidden = true) @AuthUser Long userId,

		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId,

		@Parameter(
			description = "증감 요청 바디 (없으면 delta=1)",
			schema = @Schema(implementation = KpiScoreRequestDTO.Increment.class)
		)
		@Valid @RequestBody(required = false) KpiScoreRequestDTO.Increment request
	);

	@Operation(
		summary = "내 KPI 카드 백분위 조회",
		description = "특정 KPI 카드에 대해 나의 점수와 상위/하위 백분위를 조회합니다."
	)
	@ApiErrorCodes(
		enumClass = KpiScoreErrorCode.class,
		includes = {"KPI_SCORE_NOT_FOUND"}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				examples = {
					@ExampleObject(
						name = "정상 조회",
						value = """
							{
							  "isSuccess": true,
							  "code": "COMMON200",
							  "message": "성공입니다.",
							  "result": {
							    "kpiCardId": 10,
							    "score": 75,
							    "topPercent": 20,
							    "bottomPercent": 80
							  },
							  "timestamp": "2026-02-01T02:01:32"
							}
							"""
					)
				}
			)
		)
	})
	ApiResponse<KpiScoreResponseDTO.Percentile> percentile(
		@Parameter(hidden = true)
		@AuthUser Long userId,

		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId
	);

	@Operation(
		summary = "전월 대비 KPI 점수 증감률 조회",
		description = """
			현재 KPI 누적 점수(전체 합)를 기준으로 전월 대비 증감률(%)을 반환합니다.
			- currentScore: 현재 누적 점수
			- previousScore: 전월 말 누적 점수(= currentScore - 이번 달 증가분)
			- changeRate: ((currentScore - previousScore) / previousScore) * 100, 소수점 1자리 반올림
			- previousScore가 0이면 증감률은 산정하지 않으며(changeRate = null) 비교 불가로 처리합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @Content(
				examples = {
					@ExampleObject(
						name = "비교 가능",
						value = """
							{
							  "isSuccess": true,
							  "code": "COMMON200",
							  "message": "성공입니다.",
							  "result": {
							    "year": 2026,
							    "month": 2,
							    "currentScore": 712,
							    "previousScore": 662,
							    "changeRate": 7.6
							  },
							  "timestamp": "2026-02-01T02:01:32"
							}
							"""
					),
					@ExampleObject(
						name = "비교 불가(전월 0)",
						value = """
							{
							  "isSuccess": true,
							  "code": "COMMON200",
							  "message": "성공입니다.",
							  "result": {
							    "year": 2026,
							    "month": 2,
							    "currentScore": 40,
							    "previousScore": 0,
							    "changeRate": null
							  },
							  "timestamp": "2026-02-01T02:01:32"
							}
							"""
					)
				}
			)
		)
	})
	ApiResponse<KpiScoreResponseDTO.MonthlyTotalScoreChange> getMonthlyChangeRate(
		@Parameter(hidden = true) Long userId
	);
}
