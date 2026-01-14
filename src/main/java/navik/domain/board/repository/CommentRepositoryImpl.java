package navik.domain.board.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import navik.domain.board.entity.Comment;
import navik.domain.board.entity.QComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentCustomRepository {
    private final JPAQueryFactory queryFactory;
    QComment comment = QComment.comment;

    @Override
    public Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable) {
        List<Comment> result = queryFactory.
                selectFrom(comment)
                .leftJoin(comment.parentComment) // comment와 parentComment 조인
                .where(
                        comment.parentComment.id.eq(parentCommentId)
                )
                .orderBy(
                        // 1. 그룹화(부모 댓글과 그 자식 댓글들이 같은 번호로 묶임)
                        Expressions.stringTemplate(
                                "function('COALESCE', {0}, {1})",
                                comment.parentComment.id, comment.id
                        ).asc(),
                        // 2. 그룹화 후, 부모 우선(CASE WHEN, 부모 댓글이 항상 자식보다 먼저)
                        Expressions.stringTemplate(
                                "case when {0} is null then 0 else 1 end",
                                comment.parentComment.id
                        ).asc(),
                        // 3. 순서보장을 위해 자식 중에서 오래된 순으로 정렬
                        comment.createdAt.asc()
                )
                .offset(pageable.getOffset())
                .from(comment)
                .fetch();

        // 전체 댓글 개수 나타내는 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.parentComment.id.eq(parentCommentId)
                );
        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
        // PageableExecutionUtils : 페이징 처리를 위한 최적화 유틸리티, 불필요한 전체 개수 조회 쿼리 생략
        // result : 실제 DB에서 가져온 현재 페이지에 해당하는 데이터 리스트
        // ::fetchone : 필요할 때만 이 커리 실행
    }
}
