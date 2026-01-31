package navik.domain.goal.converter;

import java.util.List;

import navik.domain.goal.dto.GoalRequestDTO;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.Goal;
import navik.domain.users.entity.User;

public class GoalConverter {
	public static Goal toEntity(User user, GoalRequestDTO.CreateDTO req) {
		return Goal.builder()
			.user(user)
			.content(req.getContent())
			.endDate(req.getEndDate())
			.build();
	}

	public static GoalResponseDTO.InfoDTO toInfoDto(Goal goal) {
		return GoalResponseDTO.InfoDTO.builder()
			.goalId(goal.getId())
			.content(goal.getContent())
			.endDate(goal.getEndDate())
			.status(goal.getStatus())
			.build();
	}

	public static GoalResponseDTO.PreviewDTO toPreviewDto(Goal goal) {
		return GoalResponseDTO.PreviewDTO.builder()
			.goalId(goal.getId())
			.content(goal.getContent())
			.status(goal.getStatus())
			.build();
	}

	public static GoalResponseDTO.InProgressDTO toInProgressDto(List<GoalResponseDTO.PreviewDTO> previewDTOs,
		Long totalCount) {
		return GoalResponseDTO.InProgressDTO.builder()
			.inProgressGoals(previewDTOs)
			.totalCount(totalCount)
			.build();
	}
}
