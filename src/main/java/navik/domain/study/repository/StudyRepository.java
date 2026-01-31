package navik.domain.study.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {

	@Query("""
		    SELECT s FROM Study s
		    JOIN FETCH s.studyUsers su
		    JOIN FETCH su.user
		    WHERE s.endDate = :endDate
		""")
	List<Study> findAllByEndDateWithStudyUser(@Param("endDate") LocalDate endDate);
}
