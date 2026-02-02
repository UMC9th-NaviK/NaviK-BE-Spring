package navik.domain.study.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.AttendStatus;
import navik.domain.users.entity.User;

public interface StudyUserRepository extends JpaRepository<StudyUser, Long> {

	List<StudyUser> findByUserId(Long userId);

	// 여러 스터디의 현재 참여 인원수를 한번에 조회한다
	@Query("""
		SELECT su.study.id, COUNT(su)
		FROM StudyUser su
		WHERE su.study.id IN :studyIds
		  AND su.attend = 'ACCEPTANCE'
		GROUP BY su.study.id
		""")
	List<Object[]> countParticipantsByStudyIds(@Param("studyIds") List<Long> studyIds);

	// 해당 스터디에 참여하는 인원 조회
	@Query("""
		SELECT sm.user
		FROM StudyUser sm
		WHERE sm.study.id = :studyId
		  AND sm.attend = 'ACCEPTANCE'
		  AND sm.isActive = true
		""")
	List<User> findUserByStudyId(@Param("studyId") Long studyId);

	// 평가 받은 스터디 목록 조회
	@Query("""
		SELECT su
		FROM StudyUser su
		JOIN FETCH su.study
		WHERE su.user.id = :userId
			AND su.attend = 'ACCEPTANCE'
			AND (:cursor IS NULL OR su.id < :cursor)
		ORDER BY su.id	DESC
		""")
	List<StudyUser> findMyStudiesByCursor(@Param("userId") Long userId, @Param("cursor") Long cursor,
		Pageable pageable);

	// 스터디 실제 참여 인원 수 계산
	long countByStudyIdAndAttend(Long studyId, AttendStatus attendStatus);

	// 평가 상세 조회
	Optional<StudyUser> findByUserIdAndStudyId(Long userId, Long studyId);
}
