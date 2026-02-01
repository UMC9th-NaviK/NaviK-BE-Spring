package navik.domain.goal.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.goal.converter.GoalConverter;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.Goal;
import navik.domain.goal.entity.GoalStatus;
import navik.domain.goal.repository.GoalRepository;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.global.dto.CursorResponseDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalQueryService {

	private final GoalRepository goalRepository;

	public Goal getGoal(Long goalId) {
		return goalRepository.findById(goalId).orElseThrow(
			() -> new GeneralExceptionHandler(GeneralErrorCode.ENTITY_NOT_FOUND));
	}

	public Goal getAuthorizedGoal(Long userId, Long goalId) {
		Goal goal = getGoal(goalId);

		if (!goal.getUser().getId().equals(userId)) {
			throw new GeneralExceptionHandler(AuthErrorCode.AUTH_FORBIDDEN);
		}

		return goal;
	}

	public CursorResponseDto<GoalResponseDTO.PreviewDTO> getGoals(Long userId, Long cursor, Integer size,
		String sortBy) {
		PageRequest pageRequest = PageRequest.of(0, size);

		Slice<Goal> goalSlice = switch (sortBy) {
			case "RECENT" -> cursor == null ?
				goalRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageRequest)
				: goalRepository.findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(userId, cursor, pageRequest);

			case "DEADLINE" -> cursor == null ?
				goalRepository.findByUserIdOrderByEndDateAscIdAsc(userId, pageRequest)
				: goalRepository.findByUserIdAndIdLessThanOrderByEndDateAscIdAsc(userId, cursor, pageRequest);

			default -> throw new GeneralExceptionHandler(GeneralErrorCode.INVALID_SORT_OPTION);
		};

		Slice<GoalResponseDTO.PreviewDTO> previewSlice = goalSlice.map(GoalConverter::toPreviewDto);

		Long nextCursor =
			goalSlice.hasNext() ? goalSlice.getContent().get(goalSlice.getContent().size() - 1).getId() : null;

		return new CursorResponseDto<>(previewSlice, String.valueOf(nextCursor));
	}

	public GoalResponseDTO.InProgressDTO getInProgressGoals(Long userId) {

		List<GoalStatus> statuses = List.of(GoalStatus.NONE, GoalStatus.IN_PROGRESS);
		List<Goal> goals = goalRepository.findTop3ByUserIdAndStatusInOrderByEndDateAscIdAsc(userId, statuses);
		Long totalCount = goalRepository.countByUserIdAndStatusIn(userId, statuses);

		List<GoalResponseDTO.InfoDTO> infos = goals.stream()
			.map(GoalConverter::toInfoDto)
			.toList();

		return GoalConverter.toInProgressDto(infos, totalCount);
	}
}
