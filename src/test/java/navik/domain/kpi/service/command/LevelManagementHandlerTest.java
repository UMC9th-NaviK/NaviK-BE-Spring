package navik.domain.kpi.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.kpi.event.KpiScoreUpdatedEvent;
import navik.domain.kpi.event.policy.LevelPolicy;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@ExtendWith(MockitoExtension.class)
class LevelManagementHandlerTest {

	@Mock
	UserQueryService userQueryService;

	@Mock
	LevelPolicy levelPolicy;

	@Mock
	KpiScoreRepository kpiScoreRepository;

	@InjectMocks
	LevelManagementHandler handler;

	@Test
	@DisplayName("총점이 레벨업 기준을 넘으면 유저 레벨이 업데이트된다")
	void levelUp_whenScoreExceedsThreshold() {
		Long userId = 1L;
		User user = mock(User.class);
		given(user.getLevel()).willReturn(2);

		given(userQueryService.getUser(userId)).willReturn(user);
		given(kpiScoreRepository.sumTotalScore(userId)).willReturn(500L);
		given(levelPolicy.calculateLevel(500L)).willReturn(3);

		handler.handleLevelUpdate(new KpiScoreUpdatedEvent(userId));

		then(user).should().updateLevel(3);
	}

	@Test
	@DisplayName("총점이 레벨업 기준에 미달하면 레벨이 변경되지 않는다")
	void noLevelUp_whenScoreBelowThreshold() {
		Long userId = 1L;
		User user = mock(User.class);
		given(user.getLevel()).willReturn(3);

		given(userQueryService.getUser(userId)).willReturn(user);
		given(kpiScoreRepository.sumTotalScore(userId)).willReturn(400L);
		given(levelPolicy.calculateLevel(400L)).willReturn(3);

		handler.handleLevelUpdate(new KpiScoreUpdatedEvent(userId));

		then(user).should(never()).updateLevel(anyInt());
	}
}
