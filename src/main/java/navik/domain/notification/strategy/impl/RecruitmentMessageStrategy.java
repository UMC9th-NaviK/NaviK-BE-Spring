package navik.domain.notification.strategy.impl;

import navik.domain.goal.entity.Goal;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.strategy.NotificationMessageStrategy;
import navik.domain.recruitment.entity.Recruitment;
import org.springframework.stereotype.Component;

@Component
public class RecruitmentMessageStrategy implements NotificationMessageStrategy {

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.RECRUITMENT;
    }

    @Override
	public String createDeadlineMessage(Notifiable target, long daysLeft) {
		Recruitment recruitment = (Recruitment) target;
        // todo: 공고 - 유저 어떻게 매핑?
		return String.format("%s님, 추천 공고 [%s],[%s] 공고 마감 D-%d일 전입니다.",daysLeft);
	}
}