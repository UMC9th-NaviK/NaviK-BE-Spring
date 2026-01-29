package navik.domain.kpi.controller.docs;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

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
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성/초기화 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "EMPTY_KPI_SCORES / INVALID_KPI_SCORE_REQUEST / DUPLICATED_KPI_CARD_ID"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KPI_CARD_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
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
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "증감 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KPI_SCORE_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
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
		description = "특정 KPI 카드에 대해 나의 점수 및 상위/하위 백분위를 조회합니다."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KPI_SCORE_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
	})
	ApiResponse<KpiScoreResponseDTO.Percentile> percentile(
		@Parameter(hidden = true) @AuthUser Long userId,

		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId
	);
}
