package navik.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.notification.entity.RecommendedRecruitment;

public interface RecommendedRecruitmentRepository
	extends JpaRepository<RecommendedRecruitment, Long> {

	@Query("""
		SELECT rr FROM RecommendedRecruitment rr
		JOIN FETCH rr.user
		JOIN FETCH rr.recruitment
		WHERE rr.id = :id
		""")
	Optional<RecommendedRecruitment> findByIdWithUserAndRecruitment(@Param("id") Long id);

	@Query("SELECT rr.id FROM RecommendedRecruitment rr")
	List<Long> findAllIds();
}
