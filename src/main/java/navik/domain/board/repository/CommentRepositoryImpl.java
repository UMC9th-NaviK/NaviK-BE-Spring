package navik.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.board.entity.Comment;
import navik.domain.board.entity.QComment;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentCustomRepository {
	private final JPAQueryFactory queryFactory;
	QComment comment = QComment.comment;
	QComment subComment = new QComment("subComment");

	@Override
	public Long countActiveComments(Long boardId) {
		return queryFactory
			.select(comment.count())
			.from(comment)
			.where(
				comment.board.id.eq(boardId),
				comment.isDeleted.isFalse()
			)
			.fetchOne();
	}

	@Override
	public Slice<Comment> findByBoardId(Long boardId, Pageable pageable) {
		List<Comment> result = queryFactory
			.selectFrom(comment)
			.leftJoin(comment.parentComment).fetchJoin()
			.leftJoin(comment.user).fetchJoin()
			.where(
				comment.board.id.eq(boardId),
				// 조회하는 조건은 삭제되지 않았거나, 삭제되었지만 대댓글이 존재하는 경우 (대댓글 존재하지 않으면 조회x)
				comment.isDeleted.isFalse() // 삭제되지 않으면 모두 조회
					.or(
						comment.isDeleted.isTrue()// 삭제된 댓글 중
							.and(comment.parentComment.isNull()) // 부모 댓글인 경우만
							.and(
								JPAExpressions.selectOne()
									.from(subComment)
									.where(subComment.parentComment.id.eq(comment.id),
										subComment.isDeleted.isFalse())
									.exists()
							)
					)
			)
			.orderBy(
				// 1. 그룹화(부모 댓글과 그 자식 댓글들이 같은 번호로 묶임)
				Expressions.numberTemplate(Long.class, "COALESCE({0}, {1})",
					comment.parentComment.id, comment.id).asc(),

				// 2. 그룹화 후, 부모 우선(CASE WHEN, 부모 댓글이 항상 자식보다 먼저)
				Expressions.numberTemplate(Integer.class,
					"CASE WHEN {0} is NULL THEN 0 ELSE 1 END",
					comment.parentComment.id).asc(),

				// 순서보장을 위해 자식 중에서 오래된 순으로 정렬
				comment.createdAt.asc()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = false;
		if (result.size() > pageable.getPageSize()) {
			result.remove(pageable.getPageSize());
			hasNext = true;
		}

		return new SliceImpl<>(result, pageable, hasNext);
	}
}
