package navik.domain.study.repository;

import java.util.List;

import navik.domain.kpi.entity.KpiCard;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.StudyRole;

public interface StudyCustomRepository {
	List<StudyUser> findMyStudyByCursor(Long userId, StudyRole role, Long cursor, int pageSize);

	List<KpiCard> findByJobNameWithCursor(String jobName, Long cursor, int pageSize);

	List<Study> findRecommendedStudyByKpi(List<Long> weaknessKpiIds, List<Long> excludeStudyIds, Long cursor,
		int pageSize);

	List<StudyUser> findApplicants(Long studyId, Long cursor, int pageSize);

	List<Study> findRecommendedStudyBySingleKpi(Long kpiId, List<Long> excludeStudyIds, Long cursor, int pageSize);
}
