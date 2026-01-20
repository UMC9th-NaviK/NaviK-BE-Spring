package navik.domain.study.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.study.entity.QStudy;
import navik.domain.study.entity.QStudyUser;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.StudyRole;

@Repository
@RequiredArgsConstructor
public class StudyCustomRepositoryImpl implements StudyCustomRepository {
	private final JPAQueryFactory queryFactory;

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

	private BooleanExpression roleEq(StudyRole role) {
		return role != null ? QStudyUser.studyUser.role.eq(role) : null;
	}

	private BooleanExpression ltStudyUserId(Long lastId) {
		return lastId != null ? QStudyUser.studyUser.id.lt(lastId) : null;
	}
}
