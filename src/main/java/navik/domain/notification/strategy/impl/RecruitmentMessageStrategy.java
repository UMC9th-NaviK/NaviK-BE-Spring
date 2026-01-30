package navik.domain.notification.strategy.impl;

import org.springframework.stereotype.Component;

import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.strategy.NotificationMessageStrategy;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.users.entity.User;

@Component
public class RecruitmentMessageStrategy implements NotificationMessageStrategy {

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.RECRUITMENT;
	}

	@Override
	public String createDeadlineMessage(User user, Notifiable target, long daysLeft) {
		Recruitment recruitment = (Recruitment)target;
		return String.format("%s님, 추천 공고 [%s] 공고 마감 D-%d일 전입니다.", user.getName(), recruitment.getTitle(), daysLeft);
	}
}