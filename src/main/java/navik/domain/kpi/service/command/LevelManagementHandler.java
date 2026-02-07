package navik.domain.kpi.service.command;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.event.KpiScoreUpdatedEvent;
import navik.domain.kpi.event.policy.LevelPolicy;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Component
@RequiredArgsConstructor
public class LevelManagementHandler {
	private final UserQueryService userQueryService;
	private final LevelPolicy levelPolicy;
	private final KpiScoreRepository kpiScoreRepository;

	@TransactionalEventListener
	@Transactional
	public void handleLevelUpdate(KpiScoreUpdatedEvent event) {
		User user = userQueryService.getUser(event.userId());

		int previousLevel = user.getLevel();
		int newLevel = levelPolicy.calculateLevel(kpiScoreRepository.sumTotalScore(event.userId()));

		isLeveledUp(newLevel, previousLevel, user);
		//todo: 레벨업시 이벤트...?
	}

	private static boolean isLeveledUp(int newLevel, int previousLevel, User user) {
		boolean leveledUp = newLevel > previousLevel;
		if (leveledUp) {
			user.updateLevel(newLevel);
		}
		return leveledUp;
	}
}
