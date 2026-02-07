package navik.domain.board.repository.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import navik.domain.board.entity.Comment;

@Repository
public interface CommentCustomRepository {
	Long countActiveComments(Long boardId);

	Slice<Comment> findByBoardId(Long boardId, Pageable pageable);
}
