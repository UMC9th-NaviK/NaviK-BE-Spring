package navik.domain.study.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {

	@Query("""
			SELECT s.id FROM Study s
			WHERE s.endDate >= :start AND s.endDate < :end
		""")
	List<Long> findAllIdsByEndDateBetweenWithStudyUser(LocalDateTime start, LocalDateTime end);

	@Query("""
			SELECT s FROM Study s
			JOIN FETCH s.studyUsers su
			JOIN FETCH su.user
			WHERE s.id = :id
		""")
	Optional<Study> findByIdWithStudyUser(@Param("id") Long id);
}
