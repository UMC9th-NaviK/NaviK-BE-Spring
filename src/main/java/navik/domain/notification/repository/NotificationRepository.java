package navik.domain.notification.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByUserIdAndCreatedAtGreaterThanEqual(Long userId, LocalDateTime createdAt);

	List<Long> findAllIdsByCreatedAtBefore(LocalDate createdAt);
}
