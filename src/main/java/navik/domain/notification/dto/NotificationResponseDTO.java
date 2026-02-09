package navik.domain.notification.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import navik.domain.notification.enums.NotificationType;

public class NotificationResponseDTO {

	@Getter
	@Builder
	public static class Notification {
		private Long notificationId;
		private String content;
		private LocalDateTime createdAt;
		private boolean isRead;
		private NotificationType notificationType;
		private String additionalInfoPerNotificationType;
	}
}
