package navik.domain.notification.strategy.impl;

import navik.domain.notification.entity.NotificationType;

import org.springframework.stereotype.Component;

import navik.domain.goal.entity.Goal;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.strategy.NotificationMessageStrategy;

@Component
public class GoalMessageStrategy implements NotificationMessageStrategy {

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.GOAL;
	}

	@Override
	public String createDeadlineMessage(Notifiable target, long daysLeft) {
		Goal goal = (Goal)target;
		return String.format("'%s' 마감까지 D-%d일 남았습니다.", goal.getContent(), daysLeft);
	}
}