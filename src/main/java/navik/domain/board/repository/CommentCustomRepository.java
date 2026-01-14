package navik.domain.board.repository;

import navik.domain.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentCustomRepository {
    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);
}
