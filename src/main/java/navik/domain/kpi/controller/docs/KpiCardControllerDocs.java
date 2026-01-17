package navik.domain.kpi.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.enums.KpiCardType;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "KPI Card", description = "KPI 카드 조회 API")
public interface KpiCardControllerDocs {

	@Operation(
		summary = "KPI 카드 목록 조회",
		description = "jobId에 해당하는 KPI 카드 그리드 목록을 조회합니다."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "JOB_NOT_FOUND"
		)
	})
	ApiResponse<List<GridItem>> getKpiCards(
		@Parameter(description = "직무 ID", example = "1", required = true)
		@RequestParam Long jobId
	);

	@Operation(
		summary = "KPI 카드 상세 조회(타입별)",
		description = "kpiCardId와 type(strong/weak)에 해당하는 상세를 조회합니다."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "INVALID_TYPE_VALUE"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "KPI_CARD_NOT_FOUND"
		)
	})
	ApiResponse<KpiCardResponseDTO.Detail> getKpiCardDetail(
		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId,

		@Parameter(
			description = "카드 타입",
			required = true,
			schema = @Schema(allowableValues = {"strong", "weak"}),
			example = "strong"
		)
		@RequestParam KpiCardType type
	);

	@Operation(
		summary = "KPI 카드 상세 조회(전체)",
		description = "kpiCardId에 해당하는 strong/weak 전체 상세를 조회합니다."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "KPI_CARD_NOT_FOUND"
		)
	})
	ApiResponse<KpiCardResponseDTO.AllDetail> getKpiCardAllDetail(
		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId
	);

	@Operation(
		summary = "상위 3개 KPI 카드 조회",
		description = "사용자 기준 상위 3개 KPI 카드를 조회합니다."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "인증 실패"
		)
	})
	ApiResponse<List<KpiCardResponseDTO.GridItem>> top(
		@Parameter(hidden = true) @AuthUser Long userId
	);

	@Operation(
		summary = "하위 3개 KPI 카드 조회",
		description = "사용자 기준 하위 3개 KPI 카드를 조회합니다."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "인증 실패"
		)
	})
	ApiResponse<List<KpiCardResponseDTO.GridItem>> bottom(
		@Parameter(hidden = true) @AuthUser Long userId
	);

}
