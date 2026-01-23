package navik.domain.study.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.entity.QKpiCard;
import navik.domain.study.entity.QStudy;
import navik.domain.study.entity.QStudyKpi;
import navik.domain.study.entity.QStudyUser;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.RecruitmentStatus;
import navik.domain.study.enums.StudyRole;

@Repository
@RequiredArgsConstructor
public class StudyCustomRepositoryImpl implements StudyCustomRepository {
	private final JPAQueryFactory queryFactory;
	private final QKpiCard kpiCard = QKpiCard.kpiCard;

	/**
	 * 나의 스터디 조회
	 * @param userId
	 * @param role
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<StudyUser> findMyStudyByCursor(Long userId, StudyRole role, Long cursor, int pageSize) {
		QStudyUser studyUser = QStudyUser.studyUser;
		QStudy study = QStudy.study;

		return queryFactory
			.selectFrom(studyUser)
			.join(studyUser.study, study).fetchJoin() // Study 정보 함께 조회
			.where(
				studyUser.user.id.eq(userId), // 내 스터디만
				roleEq(role),                 // 리더/멤버 필터
				ltStudyUserId(cursor)        // 커서 기반 페이징
			)
			.orderBy(studyUser.id.desc())    // 최신 가입 순
			.limit(pageSize + 1)
			.fetch();
	}

	/**
	 * 직무에 따른 KPI 카드 조회
	 * @param jobName
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<KpiCard> findByJobNameWithCursor(String jobName, Long cursor, int pageSize) {
		return queryFactory
			.selectFrom(kpiCard)
			.where(
				kpiCard.job.name.eq(jobName),
				ltCursorId(cursor)
			)
			.orderBy(kpiCard.id.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	/**
	 * 약점 KPI와 스터디 KPI 매핑하여 스터디 추천
	 * @param weaknessKpiIds
	 * @param excludeStudyIds
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<Study> findRecommendedStudyByKpi(List<Long> weaknessKpiIds, List<Long> excludeStudyIds, Long cursor,
		int pageSize) {
		QStudy study = QStudy.study;
		QStudyKpi studyKpi = QStudyKpi.studyKpi;
		QStudyUser studyUser = QStudyUser.studyUser;

		return queryFactory
			.selectFrom(study)
			.join(studyKpi).on(studyKpi.study.eq(study))
			.where(
				studyKpi.kpiCard.id.in(weaknessKpiIds), // 약점 KPI 중 하나라도 포함되어야 함
				study.id.notIn(excludeStudyIds), // 이미 참여중인 스터디 제외되어야 함
				study.recruitmentStatus.eq(RecruitmentStatus.RECURRING), // 모집중인 스터디이어야 함

				JPAExpressions.select(studyUser.count()) // 현재 참여 인원 < 최대 수용인원
					.from(studyUser)
					.where(studyUser.study.eq(study))
					.lt(study.capacity.longValue()),

				ltStudyUserId(cursor)
			)
			.distinct()
			.orderBy(study.id.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	private BooleanExpression roleEq(StudyRole role) {
		return role != null ? QStudyUser.studyUser.role.eq(role) : null;
	}

	private BooleanExpression ltStudyUserId(Long lastId) {
		return lastId != null ? QStudyUser.studyUser.id.lt(lastId) : null;
	}

	private BooleanExpression ltCursorId(Long cursor) {
		return cursor != null ? kpiCard.id.lt(cursor) : null;
	}

	private BooleanExpression ltStudyId(Long cursor) {
		return cursor != null ? QStudy.study.id.lt(cursor) : null;
	}
}
