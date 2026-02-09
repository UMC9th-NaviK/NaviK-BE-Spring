package navik.domain.notification.entity;

import navik.domain.notification.enums.NotificationType;

public interface Notifiable {
	NotificationType getNotificationType();

	Long getNotifiableId();

	boolean isCompleted();
}
