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
			.title(req.getTitle())
			.content(req.getContent())
			.endDate(req.getEndDate())
			.build();
	}

	public static GoalResponseDTO.InfoDTO toInfoDto(Goal goal) {
		return GoalResponseDTO.InfoDTO.builder()
			.goalId(goal.getId())
			.title(goal.getTitle())
			.content(goal.getContent())
			.endDate(goal.getEndDate())
			.status(goal.getStatus())
			.build();
	}

	public static GoalResponseDTO.PreviewDTO toPreviewDto(Goal goal) {
		return GoalResponseDTO.PreviewDTO.builder()
			.goalId(goal.getId())
			.title(goal.getTitle())
			.status(goal.getStatus())
			.build();
	}

	public static GoalResponseDTO.InProgressDTO toInProgressDto(List<GoalResponseDTO.InfoDTO> infoDTOS,
		Long totalCount) {
		return GoalResponseDTO.InProgressDTO.builder()
			.inProgressGoals(infoDTOS)
			.totalCount(totalCount)
			.build();
	}
}
