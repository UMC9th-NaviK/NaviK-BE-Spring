package navik.domain.notification.config;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import navik.domain.notification.enums.NotificationType;

@Component
public class NotificationConfig {

	private static final Map<NotificationType, List<Integer>> DEADLINE_NOTIFICATION_DAYS = Map.of(
		NotificationType.GOAL, List.of(1, 3, 7),
		NotificationType.RECRUITMENT, List.of(1, 5)
	);

	public List<Integer> getNotificationDays(NotificationType type) {
		return DEADLINE_NOTIFICATION_DAYS.getOrDefault(type, List.of());
	}
}
