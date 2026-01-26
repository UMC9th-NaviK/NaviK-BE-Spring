package navik.domain.notification.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.goal.entity.Goal;
import navik.domain.goal.repository.GoalRepository;
import navik.domain.notification.config.NotificationConfig;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.service.NotificationCommandService;
import navik.domain.notification.service.NotificationFacade;
import navik.domain.users.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final GoalRepository goalRepository;
	private final NotificationCommandService notificationCommandService;
	private final NotificationConfig notificationConfig;
	private final NotificationFacade notificationFacadeService;
	private final UserRepository userRepository;

	/**
	 * 매일 오전 9시에 알림 대상 목표의 마감일을 체크하여 D-day 알림 생성
	 * NotificationConfig에 설정된 날짜(예: D-1, D-3, D-7)에 해당하는 목표만 조회
	 */
	@Scheduled(cron = "0 0 9 * * *")
	public void checkGoalDeadlines() {

		List<Integer> notificationDays = notificationConfig.getNotificationDays(NotificationType.GOAL);

		List<LocalDate> targetEndDates = notificationDays.stream()
			.map(days -> LocalDate.now().plusDays(days))
			.collect(Collectors.toList());

		List<Goal> targetGoals = goalRepository.findByEndDateIn(targetEndDates);

		for (Goal goal : targetGoals) {
			try {
				notificationCommandService.createDeadlineNotification(
					goal.getUser().getId(),
					goal,
					goal.getEndDate()
				);
			} catch (Exception e) {
				log.error("❌ 알림 생성 실패 - Goal ID: {}, User ID: {}", goal.getId(), goal.getUser().getId(), e);
			}
		}
	}

	@Scheduled(cron = "0 0 8 * * *")
	public void checkStudyCompleted() {
		LocalDate today = LocalDate.now();
		//todo
		// 1. 오늘 종료된 스터디들에 대해 : List<Study>
		// 2. 스터디에 참여한 사용자들에 대해 : List<StudyUser>
		// 3. notificationCommandService.createCompletionNotification(userId, study);

	}

	/**
	 * 매일 오전 10시 모든 유저에 대해 추천 공고 1건을 조회하고
	 * D-1 or D-5 남은 경우 개별 알림을 생성합니다.
	 */
	@Scheduled(cron = "0 0 10 * * *")
	public void checkRecommenedRecruitment() {

		// 메모리 고려 id만 가져오고, 유저 개별 트랜잭션을 타도록 facade
		userRepository.findAllIds().forEach(
			userId -> {
				try {
					notificationFacadeService.checkRecommendedRecruitment(userId);
				} catch (Exception e) {
					log.error("추천 공고 알림 생성 실패 - User ID: {}", userId, e);
				}
			}
		);
	}
}
