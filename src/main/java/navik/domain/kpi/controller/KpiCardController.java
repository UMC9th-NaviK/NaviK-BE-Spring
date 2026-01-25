package navik.domain.kpi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.controller.docs.KpiCardControllerDocs;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.enums.KpiCardType;
import navik.domain.kpi.service.query.KpiCardQueryService;
import navik.domain.kpi.service.query.KpiScoreQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kpi-cards")
public class KpiCardController implements KpiCardControllerDocs {

	private final KpiCardQueryService kpiCardQueryService;
	private final KpiScoreQueryService kpiScoreQueryService;

	@GetMapping
	@Override
	public ApiResponse<List<GridItem>> getKpiCards(@RequestParam Long jobId) {
		List<GridItem> cards = kpiCardQueryService.getAllCardsByJob(jobId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, cards);
	}

	@GetMapping("/{kpiCardId}")
	@Override
	public ApiResponse<KpiCardResponseDTO.Detail> getKpiCardDetail(
		@PathVariable Long kpiCardId,
		@RequestParam KpiCardType type
	) {
		return ApiResponse.onSuccess(
			GeneralSuccessCode._OK,
			kpiCardQueryService.getCardDetail(kpiCardId, type)
		);
	}

	@GetMapping("/{kpiCardId}/all")
	@Override
	public ApiResponse<KpiCardResponseDTO.AllDetail> getKpiCardAllDetail(
		@PathVariable Long kpiCardId
	) {
		return ApiResponse.onSuccess(
			GeneralSuccessCode._OK,
			kpiCardQueryService.getCardAllDetail(kpiCardId)
		);
	}

	@GetMapping("/top")
	@Override
	public ApiResponse<List<KpiCardResponseDTO.GridItem>> top(@AuthUser Long userId) {
		return ApiResponse.onSuccess(
			GeneralSuccessCode._OK,
			kpiScoreQueryService.getTop3KpiCards(userId)
		);
	}

	@GetMapping("/bottom")
	@Override
	public ApiResponse<List<KpiCardResponseDTO.GridItem>> bottom(@AuthUser Long userId) {
		return ApiResponse.onSuccess(
			GeneralSuccessCode._OK,
			kpiScoreQueryService.getBottom3KpiCards(userId)
		);
	}
}
