package navik.domain.notification.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

public class NotificationResponseDTO {

	@Getter
	@Builder
	public static class Notification {
		private Long notificationId;
		private String content;
		private LocalDateTime createdAt;
		private boolean isRead;
	}
}
