package navik.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.notification.entity.RecommendedRecruitmentNotification;

public interface RecommendedRecruitmentNotificationRepository
	extends JpaRepository<RecommendedRecruitmentNotification, Long> {
}
