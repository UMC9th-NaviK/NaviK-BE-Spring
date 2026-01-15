package navik.domain.growthLog.controller;

import java.time.YearMonth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.converter.GrowthLogConverter;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.service.command.GrowthLogUserInputService;
import navik.domain.growthLog.service.query.GrowthLogQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.PageResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/growth-logs")
public class GrowthLogController {

	private final GrowthLogUserInputService growthLogUserInputService;
	private final GrowthLogQueryService growthLogQueryService;

	@PostMapping
	public ApiResponse<GrowthLogResponseDTO.Id> create(
		@RequestBody @Valid GrowthLogRequestDTO.CreateUserInput request
	) {
		Long id = growthLogUserInputService.create(request);

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, new GrowthLogResponseDTO.Id(id));
	}

	@GetMapping("/monthly")
	public ApiResponse<PageResponseDto<GrowthLogResponseDTO.ListItem>> getMonthlyGrowthLogs(
		@AuthUser Long userId,
		@RequestParam YearMonth yearMonth,
		@RequestParam(required = false) GrowthType type,
		Pageable pageable
	) {
		Page<GrowthLog> logs =
			growthLogQueryService.getMonthlyLogs(
				userId,
				yearMonth,
				type,
				pageable
			);

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, GrowthLogConverter.toPageResponse(logs));
	}

}
