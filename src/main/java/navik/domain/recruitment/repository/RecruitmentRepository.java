package navik.domain.recruitment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.recruitment.entity.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

	@Query(value = """
		SELECT r.*
		FROM recruitments r
		JOIN positions p ON r.id = p.recruitment_id
		JOIN (
			SELECT 
				pk.position_id, SUM(1 - (ke.embedding <=> ae.embedding)) as total_score
			FROM position_kpis pk
			JOIN position_kpi_embeddings ke ON pk.id = ke.position_kpi_id
			JOIN positions p ON pk.position_id = p.id
			JOIN abilities a ON a.user_id = :userId
			JOIN ability_embeddings ae ON a.id = ae.ability_id
			WHERE p.job_id = :jobId 
					  AND (p.education_type = :educationType OR p.education_type IS NULL)
					  AND (p.experience_type = :experienceType OR p.experience_type IS NULL)
					  AND (p.major_type = :majorType OR p.major_type IS NULL)
			GROUP BY pk.position_id
		) pos_agg ON p.id = pos_agg.position_id
		WHERE (r.end_date >= :now OR r.end_date IS NULL)
		GROUP BY r.id
		ORDER BY MAX(pos_agg.total_score) DESC
		LIMIT 5
		""", nativeQuery = true)
	List<Recruitment> searchTop5RecruitmentsByUserId(
		@Param("userId") Long userId,
		@Param("jobId") Long jobId,
		@Param("educationType") String educationType,
		@Param("experienceType") String experienceType,
		@Param("majorType") String majorType,
		@Param("now") LocalDateTime now);
}
