package navik.domain.growthLog.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.service.command.GrowthLogUserInputService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/growth-logs")
public class GrowthLogController {

	private final GrowthLogUserInputService growthLogUserInputService;

	@PostMapping
	public ApiResponse<GrowthLogResponseDTO.Id> create(
		@RequestBody @Valid GrowthLogRequestDTO.CreateUserInput request
	) {
		Long id = growthLogUserInputService.create(request);

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, new GrowthLogResponseDTO.Id(id));
	}

}
