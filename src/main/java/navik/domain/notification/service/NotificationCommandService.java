package navik.domain.notification.service;

import jakarta.transaction.Transactional;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.Notification;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.repository.NotificationRepository;
import navik.domain.notification.strategy.NotificationMessageStrategy;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationCommandService {

    private final UserQueryService userQueryService;
    private final NotificationRepository notificationRepository;

    private final Map<NotificationType, NotificationMessageStrategy> strategyMap;

    public NotificationCommandService(
            UserQueryService userQueryService,
            NotificationRepository notificationRepository,
            List<NotificationMessageStrategy> strategies
    ) {
        this.userQueryService = userQueryService;
        this.notificationRepository = notificationRepository;

        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        NotificationMessageStrategy::getNotificationType,
                        strategy -> strategy
                ));
    }

    public void createNotification(Long userId, Notifiable target, String content){

        User user = userQueryService.getUser(userId);

        Notification notification = Notification.builder()
                .user(user)
                .type(target.getNotificationType())
                .relateId(target.getNotifiableId())
                .content(content)
                .build();

        notificationRepository.save(notification);
    }
    public void createDeadlineNotification(Long userId, Notifiable target, LocalDate endDate) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        NotificationMessageStrategy strategy = strategyMap.get(target.getNotificationType());
        String content = strategy.createDeadlineMessage(target, daysLeft);
        createNotification(userId, target, content);
    }

    public void createCompletionNotification(Long userId, Notifiable target) {
        NotificationMessageStrategy strategy = strategyMap.get(target.getNotificationType());
        String content = strategy.createDeadlineMessage(target, 0);
        createNotification(userId, target, content);
    }
}
