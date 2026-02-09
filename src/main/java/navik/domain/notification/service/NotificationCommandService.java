package navik.domain.notification.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.Notification;
import navik.domain.notification.exception.code.NotificationErrorCode;
import navik.domain.notification.repository.NotificationRepository;
import navik.domain.notification.strategy.NotificationMessageStrategy;
import navik.domain.notification.strategy.NotificationMessageStrategyFactory;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

	private final UserQueryService userQueryService;
	private final NotificationRepository notificationRepository;
	private final NotificationMessageStrategyFactory strategyFactory;

	public void createNotification(Long userId, Notifiable target, String content) {
		User user = userQueryService.getUser(userId);

		Notification notification = Notification.builder()
			.user(user)
			.type(target.getNotificationType())
			.relateId(target.getNotifiableId())
			.content(content)
			.additionalInfo(target.getAdditionalInfo())
			.build();

		notificationRepository.save(notification);
	}

	public void createDeadlineNotification(Long userId, Notifiable target, LocalDate endDate) {
		User user = userQueryService.getUser(userId);
		long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
		NotificationMessageStrategy strategy = strategyFactory.getStrategy(target.getNotificationType());
		String content = strategy.createMessage(user, target, daysLeft);
		createNotification(userId, target, content);
	}

	public void createCompletionNotification(Long userId, Notifiable target) {
		User user = userQueryService.getUser(userId);
		NotificationMessageStrategy strategy = strategyFactory.getStrategy(target.getNotificationType());
		String content = strategy.createMessage(user, target, 0);
		createNotification(userId, target, content);
	}

	public void deleteNotification(Long notificationId) {

		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new GeneralException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

		notificationRepository.delete(notification);
	}

	public void readNotification(Long userId, Long notificationId) {

		User user = userQueryService.getUser(userId);

		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new GeneralException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

		if (!notification.getUser().equals(user))
			throw new GeneralException(NotificationErrorCode.NOTIFICATION_NOT_OWNER);

		notification.setIsRead();
	}
}
