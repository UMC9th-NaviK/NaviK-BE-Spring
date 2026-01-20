package navik.domain.study.repository;

import java.util.List;

import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.StudyRole;

public interface StudyCustomRepository {
	List<StudyUser> findMyStudyByCursor(Long userId, StudyRole role, Long lastId, int pageSize);
}
