package navik.domain.study.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {

	@Query("""
			SELECT s.id FROM Study s
			WHERE s.endDate = :endDate
		""")
	List<Long> findAllIdsByEndDateWithStudyUser(@Param("endDate") LocalDate endDate);

	@Query("""
			SELECT s FROM Study s
			JOIN FETCH s.studyUsers su
			JOIN FETCH su.user
		""")
	Optional<Study> findByIdWithStudyUser(@Param("id") Long id);
}
