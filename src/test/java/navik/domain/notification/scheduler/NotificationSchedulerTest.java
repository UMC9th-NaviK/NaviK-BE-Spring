package navik.domain.notification.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.goal.entity.Goal;
import navik.domain.goal.entity.GoalStatus;
import navik.domain.goal.repository.GoalRepository;
import navik.domain.notification.config.NotificationConfig;
import navik.domain.notification.enums.NotificationType;
import navik.domain.notification.service.NotificationCommandService;
import navik.domain.users.entity.User;
import navik.domain.users.enums.Role;
import navik.domain.users.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationScheduler 단위 테스트")
class NotificationSchedulerTest {

	@Mock
	private GoalRepository goalRepository;

	@Mock
	private NotificationCommandService notificationCommandService;

	@Mock
	private NotificationConfig notificationConfig;

	@InjectMocks
	private NotificationScheduler notificationScheduler;

	@Test
	@DisplayName("목표 마감일 체크 - D-1, D-3, D-7 알림 생성")
	void checkGoalDeadlines_정상동작() {
		// given
		List<Integer> notificationDays = List.of(1, 3, 7);
		when(notificationConfig.getNotificationDays(NotificationType.GOAL))
			.thenReturn(notificationDays);

		User user = createTestUser(1L);
		Goal goal1 = createTestGoal(1L, user, LocalDate.now().plusDays(1));
		Goal goal2 = createTestGoal(2L, user, LocalDate.now().plusDays(3));
		Goal goal3 = createTestGoal(3L, user, LocalDate.now().plusDays(7));

		List<Goal> targetGoals = List.of(goal1, goal2, goal3);
		when(goalRepository.findByEndDateIn(any())).thenReturn(targetGoals);

		// when
		notificationScheduler.checkGoalDeadlines();

		// then
		verify(notificationConfig, times(1)).getNotificationDays(NotificationType.GOAL);
		verify(goalRepository, times(1)).findByEndDateIn(any());
		verify(notificationCommandService, times(3)).createDeadlineNotification(any(), any(), any());
	}

	@Test
	@DisplayName("목표 마감일 체크 - 대상 목표가 없을 때")
	void checkGoalDeadlines_대상없음() {
		// given
		List<Integer> notificationDays = List.of(1, 3, 7);
		when(notificationConfig.getNotificationDays(NotificationType.GOAL))
			.thenReturn(notificationDays);

		when(goalRepository.findByEndDateIn(any())).thenReturn(List.of());

		// when
		notificationScheduler.checkGoalDeadlines();

		// then
		verify(notificationCommandService, never()).createDeadlineNotification(any(), any(), any());
	}

	@Test
	@DisplayName("목표 마감일 체크 - 올바른 날짜로 조회하는지 검증")
	void checkGoalDeadlines_날짜검증() {
		// given
		List<Integer> notificationDays = List.of(1, 3, 7);
		when(notificationConfig.getNotificationDays(NotificationType.GOAL))
			.thenReturn(notificationDays);

		when(goalRepository.findByEndDateIn(any())).thenReturn(List.of());

		// when
		notificationScheduler.checkGoalDeadlines();

		// then
		ArgumentCaptor<List<LocalDate>> dateCaptor = ArgumentCaptor.forClass(List.class);
		verify(goalRepository).findByEndDateIn(dateCaptor.capture());

		List<LocalDate> capturedDates = dateCaptor.getValue();
		assertThat(capturedDates).hasSize(3);
		assertThat(capturedDates).containsExactlyInAnyOrder(
			LocalDate.now().plusDays(1),
			LocalDate.now().plusDays(3),
			LocalDate.now().plusDays(7)
		);
	}

	@Test
	@DisplayName("목표 마감일 체크 - 알림 생성 시 올바른 파라미터 전달")
	void checkGoalDeadlines_파라미터검증() {
		// given
		List<Integer> notificationDays = List.of(1);
		when(notificationConfig.getNotificationDays(NotificationType.GOAL))
			.thenReturn(notificationDays);

		User user = createTestUser(100L);
		LocalDate endDate = LocalDate.now().plusDays(1);
		Goal goal = createTestGoal(200L, user, endDate);

		when(goalRepository.findByEndDateIn(any())).thenReturn(List.of(goal));

		// when
		notificationScheduler.checkGoalDeadlines();

		// then
		verify(notificationCommandService).createDeadlineNotification(
			eq(100L),
			eq(goal),
			eq(endDate)
		);
	}

	@Test
	@DisplayName("목표 마감일 체크 - 여러 사용자의 목표 처리")
	void checkGoalDeadlines_여러사용자() {
		// given
		List<Integer> notificationDays = List.of(1);
		when(notificationConfig.getNotificationDays(NotificationType.GOAL))
			.thenReturn(notificationDays);

		User user1 = createTestUser(1L);
		User user2 = createTestUser(2L);
		User user3 = createTestUser(3L);

		Goal goal1 = createTestGoal(1L, user1, LocalDate.now().plusDays(1));
		Goal goal2 = createTestGoal(2L, user2, LocalDate.now().plusDays(1));
		Goal goal3 = createTestGoal(3L, user3, LocalDate.now().plusDays(1));

		when(goalRepository.findByEndDateIn(any())).thenReturn(List.of(goal1, goal2, goal3));

		// when
		notificationScheduler.checkGoalDeadlines();

		// then
		verify(notificationCommandService, times(3)).createDeadlineNotification(any(), any(), any());
		verify(notificationCommandService).createDeadlineNotification(eq(1L), eq(goal1), any());
		verify(notificationCommandService).createDeadlineNotification(eq(2L), eq(goal2), any());
		verify(notificationCommandService).createDeadlineNotification(eq(3L), eq(goal3), any());
	}

	// 테스트 헬퍼 메서드
	private User createTestUser(Long id) {
		return User.builder()
			.id(id)
			.name("테스트 사용자" + id)
			.email("test" + id + "@test.com")
			.role(Role.USER)
			.socialId("social" + id)
			.socialType("google")
			.userStatus(UserStatus.ACTIVE)
			.build();
	}

	private Goal createTestGoal(Long id, User user, LocalDate endDate) {
		return Goal.builder()
			.id(id)
			.content("테스트 목표 " + id)
			.endDate(endDate)
			.status(GoalStatus.NONE)
			.user(user)
			.build();
	}
}