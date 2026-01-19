package navik.domain.board.repository;

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
	public List<Board> findAllByCursor(Long lastId, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;

		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(ltBoardId(lastId))
			.orderBy(board.id.desc())
			.limit(pageSize)
			.fetch();
	}

	@Override
	public List<Board> findByJobAndCursor(String jobName, Long lastId, int pageSize) {
		QBoard board = QBoard.board;
		QUser user = QUser.user;
		QJob job = QJob.job;

		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user, user).fetchJoin()
			.leftJoin(user.job, job).fetchJoin()
			.where(
				board.user.job.name.eq(jobName),
				ltBoardId(lastId)
			)
			.orderBy(board.id.desc())
			.limit(pageSize)
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
			.orderBy(scoreSum.desc(), board.id.desc()) // 점수 높은순 -> 최신순
			.limit(pageSize)
			.fetch();
	}

	@Override
	public List<Board> searchByKeyword(String keyword, Long lastId, int pageSize) {
		QBoard board = QBoard.board;
		return queryFactory
			.selectFrom(board)
			.leftJoin(board.user).fetchJoin()
			.where(
				keywordContains(keyword), // 검색어가 있는지 확인 (제목이나 내용에)
				ltBoardId(lastId)
			)
			.orderBy(board.id.desc())
			.limit(pageSize)
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

	private BooleanExpression ltBoardId(Long lastId) {
		return lastId == null ? null : QBoard.board.id.lt(lastId);
	}
}
