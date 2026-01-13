package navik.domain.notification.entity;

public interface Notifiable {
	NotificationType getNotificationType();

	Long getNotifiableId();

	boolean isCompleted();
}
