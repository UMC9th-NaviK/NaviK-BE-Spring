package navik.domain.board.repository.board;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.QBoard;
import navik.domain.job.entity.QJob;
import navik.domain.users.entity.QUser;

@Repository
@RequiredArgsConstructor
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Board> findAllByCursor(LocalDateTime lastCreatedAt, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;

		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(ltCreatedAt(lastCreatedAt))
			.orderBy(board.createdAt.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	@Override
	public List<Board> findByJobAndCursor(String jobName, LocalDateTime lastCreatedAt, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;

		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(
				board.user.job.name.eq(jobName),
				ltCreatedAt(lastCreatedAt)
			)
			.orderBy(board.createdAt.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	@Override
	public List<Board> findHotBoardsByCursor(Integer lastScore, Long lastId, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;

		// 좋아요 + 조회수 합계 계산
		NumberExpression<Integer> scoreSum = board.articleLikes.add(board.articleViews);

		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(cursorCondition(lastScore, lastId, scoreSum))
			.orderBy(scoreSum.desc(), board.createdAt.desc()) // 점수 높은순 -> 최신순
			.limit(pageSize + 1)
			.fetch();
	}

	@Override
	public List<Board> searchByKeyword(String keyword, LocalDateTime lastCreatedAt, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;
		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(
				keywordContains(keyword), // 검색어가 있는지 확인 (제목이나 내용에)
				ltCreatedAt(lastCreatedAt)
			)
			.orderBy(board.createdAt.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null || keyword.isEmpty())
			return null;
		return QBoard.board.articleTitle.contains(keyword)
			.or(QBoard.board.articleContent.contains(keyword));
	}

	private BooleanExpression cursorCondition(Integer lastScore, Long lastId, NumberExpression<Integer> scoreSum) {
		if (lastScore == null || lastId == null) {
			return null;
		}

		QBoard board = QBoard.board;

		// 복합 커서 조건: (현재 점수 < 마지막 점수) OR (현재 점수 == 마지막 점수 AND ID < 마지막 ID)
		return scoreSum.lt(lastScore)
			.or(scoreSum.eq(lastScore).and(board.id.lt(lastId)));
	}

	private BooleanExpression ltCreatedAtAndId(LocalDateTime lastCreatedAt, Long lastId) {
		if (lastCreatedAt == null || lastId == null) {
			return null;
		}

		QBoard board = QBoard.board;

		// 복합 커서 로직: (생성일 < 마지막 생성일) OR (생성일 == 마지막 생성일 AND ID < 마지막 ID)
		return board.createdAt.lt(lastCreatedAt)
			.or(board.createdAt.eq(lastCreatedAt).and(board.id.lt(lastId)));
	}

	private BooleanExpression ltCreatedAt(LocalDateTime lastCreatedAt) {
		// 시간이 null이면 첫 페이지이므로 조건을 걸지 않아요.
		if (lastCreatedAt == null) {
			return null;
		}

		// 단순히 마지막으로 본 시간보다 이전(미만)인 데이터만 가져옵니다.
		return QBoard.board.createdAt.lt(lastCreatedAt);
	}
}
