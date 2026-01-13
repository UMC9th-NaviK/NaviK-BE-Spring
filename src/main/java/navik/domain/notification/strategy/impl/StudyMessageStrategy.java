package navik.domain.notification.strategy.impl;

import navik.domain.notification.entity.NotificationType;
import navik.domain.study.entity.Study;

import org.springframework.stereotype.Component;

import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.strategy.NotificationMessageStrategy;
import navik.domain.recruitment.entity.Recruitment;

@Component
public class StudyMessageStrategy implements NotificationMessageStrategy {

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.STUDY;
	}

	@Override
	public String createDeadlineMessage(Notifiable target, long daysLeft) {
		Study study = (Study)target;
		return String.format("[%s] 스터디가 종료되었습니다. 평가를 남기고 성장 기록을 확인해보세요!", study.getTitle());
	}
}