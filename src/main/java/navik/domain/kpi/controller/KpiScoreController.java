package navik.domain.kpi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.kpi.controller.docs.KpiScoreControllerDocs;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO.Initialize;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO.Percentile;
import navik.domain.kpi.service.command.KpiScoreIncrementService;
import navik.domain.kpi.service.command.KpiScoreInitialService;
import navik.domain.kpi.service.query.KpiScoreQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kpi-scores")
public class KpiScoreController implements KpiScoreControllerDocs {

	private final KpiScoreInitialService kpiScoreInitialService;
	private final KpiScoreIncrementService kpiScoreIncrementService;
	private final KpiScoreQueryService kpiScoreQueryService;

	@PutMapping("/initialize")
	@Override
	public ApiResponse<Initialize> initialize(
		@AuthUser Long userId,
		@Valid @RequestBody KpiScoreRequestDTO.Initialize request
	) {
		KpiScoreResponseDTO.Initialize response =
			kpiScoreInitialService.initializeKpiScores(userId, request);

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, response);
	}

	@PatchMapping("/{kpiCardId}/increment")
	@Override
	public ApiResponse<KpiScoreResponseDTO.Increment> increment(
		@AuthUser Long userId,
		@PathVariable Long kpiCardId,
		@Valid @RequestBody(required = false) KpiScoreRequestDTO.Increment request
	) {
		var response = kpiScoreIncrementService.incrementKpiScore(userId, kpiCardId, request);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	@GetMapping("/{kpiCardId}/percentile")
	@Override
	public ApiResponse<KpiScoreResponseDTO.Percentile> percentile(
		@AuthUser Long userId,
		@PathVariable Long kpiCardId
	) {
		Percentile response = kpiScoreQueryService.getMyPercentile(userId, kpiCardId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

}
