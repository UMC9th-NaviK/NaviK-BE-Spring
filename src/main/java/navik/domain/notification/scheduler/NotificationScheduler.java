package navik.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.goal.entity.Goal;
import navik.domain.goal.repository.GoalRepository;
import navik.domain.notification.config.NotificationConfig;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.service.NotificationCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final GoalRepository goalRepository;
    private final NotificationCommandService notificationCommandService;
    private final NotificationConfig notificationConfig;

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
            notificationCommandService.createDeadlineNotification(
                    goal.getUser().getId(),
                    goal,
                    goal.getEndDate()
                );
        }
    }
}
