package navik.domain.notification.repository;

import navik.domain.notification.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
