package navik.domain.goal.service;

import navik.domain.users.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.goal.converter.GoalConverter;
import navik.domain.goal.dto.GoalRequestDTO;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.Goal;
import navik.domain.goal.entity.GoalStatus;
import navik.domain.goal.repository.GoalRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCommandService {

	private final GoalRepository goalRepository;
	private final UserQueryService userQueryService;
	private final GoalQueryService goalQueryService;
	private final UserRepository userRepository;

	public GoalResponseDTO.InfoDTO createGoal(Long userId, GoalRequestDTO.CreateDTO req) {
		User user = userQueryService.getUser(userId);

		Goal newGoal = GoalConverter.toEntity(user, req);
		goalRepository.save(newGoal);

		return GoalConverter.toInfoDto(newGoal);
	}

	public GoalResponseDTO.InfoDTO updateGoalStatus(Long userId, Long goalId, GoalStatus status) {
		Goal goal = goalQueryService.getAuthorizedGoal(userId, goalId);

		goal.updateStatus(status);

		return GoalConverter.toInfoDto(goal);
	}

	public void deleteGoal(Long userId, Long goalId) {
		Goal goal = goalQueryService.getAuthorizedGoal(userId, goalId);

		goalRepository.delete(goal);
	}
}
