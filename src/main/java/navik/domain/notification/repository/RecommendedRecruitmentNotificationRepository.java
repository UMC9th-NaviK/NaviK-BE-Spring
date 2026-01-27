package navik.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.notification.entity.RecommendedRecruitmentNotification;

public interface RecommendedRecruitmentNotificationRepository
	extends JpaRepository<RecommendedRecruitmentNotification, Long> {

	@Query("""
		SELECT rrn FROM RecommendedRecruitmentNotification rrn
		JOIN FETCH rrn.user
		JOIN FETCH rrn.recruitment
		WHERE rrn.id = :id
		""")
	Optional<RecommendedRecruitmentNotification> findByIdWithUserAndRecruitment(@Param("id") Long id);

	@Query("SELECT rrn.id FROM RecommendedRecruitmentNotification rrn")
	List<Long> findAllIds();
}
