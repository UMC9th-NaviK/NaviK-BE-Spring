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
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.enums.RecruitmentStatus;
import navik.domain.study.enums.StudyRole;
import navik.domain.users.entity.QUser;

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
				studyUser.attend.eq(AttendStatus.ACCEPTANCE),
				cursor != null ? studyUser.id.lt(cursor) : null
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
				ltKpiId(cursor)
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
		QStudyUser subStudyUser = new QStudyUser("subStudyUser");

		return queryFactory
			.selectFrom(study)
			.join(studyKpi).on(studyKpi.study.eq(study))
			.where(
				studyKpi.kpiCard.id.in(weaknessKpiIds),
				study.id.notIn(excludeStudyIds),
				study.recruitmentStatus.ne(RecruitmentStatus.CLOSED),

				JPAExpressions.select(subStudyUser.count())
					.from(subStudyUser)
					.where(
						subStudyUser.study.eq(study), // 메인 쿼리의 study와 연결
						subStudyUser.attend.eq(AttendStatus.ACCEPTANCE)
					)
					.lt(study.capacity.longValue()),

				ltStudyId(cursor)
			)
			.distinct()
			.orderBy(study.id.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	/**
	 * 스터디 신청 현항
	 * @param studyId
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<StudyUser> findApplicants(Long studyId, Long cursor, int pageSize) {
		QStudyUser studyUser = QStudyUser.studyUser;
		QUser user = QUser.user;

		return queryFactory
			.selectFrom(studyUser)
			.join(studyUser.user, user).fetchJoin()
			.where(
				studyUser.study.id.eq(studyId),
				studyUser.attend.eq(AttendStatus.WAITING), // 대기 중인 신청자만
				ltApplicantId(cursor, studyUser)
			)
			.limit(pageSize + 1)
			.orderBy(studyUser.id.asc())
			.fetch();
	}

	private BooleanExpression roleEq(StudyRole role) {
		return role != null ? QStudyUser.studyUser.role.eq(role) : null;
	}

	private BooleanExpression ltKpiId(Long cursor) {
		return cursor != null ? kpiCard.id.lt(cursor) : null;
	}

	private BooleanExpression ltStudyId(Long cursor) {
		return cursor != null ? QStudy.study.id.lt(cursor) : null;
	}

	private BooleanExpression ltApplicantId(Long cursor, QStudyUser studyUser) {
		return cursor == null ? null : studyUser.id.gt(cursor);
	}
}
