package navik.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
		Long userId,
		LocalDateTime createdAt
	);

	@Query("select n.id from Notification n where n.createdAt < :createdAt")
	List<Long> findAllIdsByCreatedAtBefore(@Param("createdAt") LocalDateTime createdAt);
}
