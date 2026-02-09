package navik.domain.notification.converter;

import navik.domain.notification.dto.NotificationResponseDTO;
import navik.domain.notification.entity.Notification;

public class NotificationConverter {

	public static NotificationResponseDTO.Notification toNotification(Notification notification) {
		return NotificationResponseDTO.Notification.builder()
			.notificationId(notification.getId())
			.content(notification.getContent())
			.createdAt(notification.getCreatedAt())
			.isRead(notification.getIsRead())
			.notificationType(notification.getType())
			.additionalInfoPerNotificationType(notification.getAdditionalInfo())
			.build();
	}
}
