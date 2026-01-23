package navik.domain.study.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.study.entity.StudyUser;

public interface StudyUserRepository extends JpaRepository<StudyUser, Long> {

	List<StudyUser> findByUserId(Long userId);

	// 여러 스터디의 현재 참여 인원수를 한번에 조회한다
	@Query("SELECT su.study.id, COUNT(su) " + "FROM StudyUser su " +
		"WHERE su.study.id IN :studyIds " + "AND su.attend = 'ACCEPTANCE' " + "GROUP BY su.study.id")
	List<Object[]> countParticipantsByStudyIds(@Param("studyIds") List<Long> studyIds);
}
