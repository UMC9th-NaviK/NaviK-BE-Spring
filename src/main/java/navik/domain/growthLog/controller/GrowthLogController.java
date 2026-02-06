package navik.domain.growthLog.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.controller.docs.GrowthLogControllerDocs;
import navik.domain.growthLog.converter.GrowthLogConverter;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.AggregateUnit;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.service.command.GrowthLogEvaluationService;
import navik.domain.growthLog.service.query.GrowthLogAggregateService;
import navik.domain.growthLog.service.query.GrowthLogQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.SliceResponseDto;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/growth-logs")
public class GrowthLogController implements GrowthLogControllerDocs {

	private final GrowthLogEvaluationService growthLogEvaluationService;
	private final GrowthLogQueryService growthLogQueryService;
	private final GrowthLogAggregateService growthLogAggregateService;

	@Override
	@PostMapping
	public ApiResponse<GrowthLogResponseDTO.CreateResult> create(
		@AuthUser Long userId,
		@RequestBody @Valid GrowthLogRequestDTO.CreateUserInput request
	) {
		GrowthLogResponseDTO.CreateResult result = growthLogEvaluationService.create(userId, request);
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, result);
	}

	@Override
	@PostMapping("/{growthLogId}/retry")
	public ApiResponse<GrowthLogResponseDTO.RetryResult> retry(
		@AuthUser Long userId,
		@PathVariable Long growthLogId
	) {
		GrowthLogResponseDTO.RetryResult result =
			growthLogEvaluationService.retry(userId, growthLogId);

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, result);
	}

	// 요약 보기
	@Override
	@GetMapping("/monthly")
	public ApiResponse<SliceResponseDto<GrowthLogResponseDTO.ListItem>> getMonthlyGrowthLogs(
		@AuthUser Long userId,
		@RequestParam YearMonth yearMonth,
		@RequestParam(required = false) GrowthType type,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Slice<GrowthLog> logs = growthLogQueryService.getMonthlyLogs(userId, yearMonth, type, pageable);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, GrowthLogConverter.toSliceResponse(logs));
	}

	@Override
	@GetMapping("/{growthLogId}")
	public ApiResponse<GrowthLogResponseDTO.Detail> getGrowthLogDetail(
		@AuthUser Long userId,
		@PathVariable Long growthLogId
	) {
		GrowthLog gl = growthLogQueryService.getDetail(userId, growthLogId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, GrowthLogConverter.toDetail(gl));
	}

	@Override
	@GetMapping("/aggregate/scores")
	public ApiResponse<List<GrowthLogResponseDTO.Point>> getGrowthScoreTimeline(
		@AuthUser Long userId,
		@RequestParam AggregateUnit unit,
		@RequestParam(required = false) GrowthType type
	) {
		return ApiResponse.onSuccess(
			GeneralSuccessCode._OK,
			growthLogAggregateService.getScoreTimeline(userId, unit, type)
		);
	}

}
