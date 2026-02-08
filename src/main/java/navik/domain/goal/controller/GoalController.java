package navik.domain.goal.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.goal.dto.GoalRequestDTO;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.GoalStatus;
import navik.domain.goal.service.GoalCommandService;
import navik.domain.goal.service.GoalQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/goals")
public class GoalController implements GoalControllerDocs {

	private final GoalQueryService goalQueryService;
	private final GoalCommandService goalCommandService;

	@ GetMapping("/{goalId}")
	public ApiResponse<GoalResponseDTO.InfoDTO> getGoal(
		@AuthUser Long userId,
		@PathVariable Long goalId
	){
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, goalQueryService.getGoalInfo(userId, goalId));
	}

	@Override
	@GetMapping("/list")
	public ApiResponse<CursorResponseDTO<GoalResponseDTO.PreviewDTO>> getGoals(
		@AuthUser Long userId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam String sortBy) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, goalQueryService.getGoals(userId, cursor, size, sortBy));
	}

	@Override
	@GetMapping("/in-progress")
	public ApiResponse<GoalResponseDTO.InProgressDTO> getInProgressGoals(@AuthUser Long userId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, goalQueryService.getInProgressGoals(userId));
	}

	@Override
	@PostMapping
	public ApiResponse<GoalResponseDTO.InfoDTO> createGoal(
		@AuthUser Long userId,
		@RequestBody @Valid GoalRequestDTO.CreateDTO req) {
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, goalCommandService.createGoal(userId, req));
	}

	@Override
	@PatchMapping("/{goalId}/status")
	public ApiResponse<GoalResponseDTO.InfoDTO> updateGoalStatus(
		@AuthUser Long userId,
		@PathVariable Long goalId,
		@RequestParam GoalStatus status) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK,
			goalCommandService.updateGoalStatus(userId, goalId, status));
	}

	@Override
	@DeleteMapping("/{goalId}")
	public ApiResponse<Void> deleteGoal(
		@AuthUser Long userId,
		@PathVariable Long goalId) {
		goalCommandService.deleteGoal(userId, goalId);
		return ApiResponse.onSuccess(GeneralSuccessCode._DELETED, null);
	}
}
