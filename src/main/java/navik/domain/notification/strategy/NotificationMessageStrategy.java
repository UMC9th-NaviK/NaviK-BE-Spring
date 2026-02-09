package navik.domain.notification.strategy;

import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.enums.NotificationType;
import navik.domain.users.entity.User;

public interface NotificationMessageStrategy {
	NotificationType getNotificationType();

	String createMessage(User user, Notifiable target, long daysLeft);
}