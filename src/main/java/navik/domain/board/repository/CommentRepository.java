package navik.domain.board.repository;

import java.util.List;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardAndIsDeletedFalse(Board board); // 지웠졌는지 확인
    long countCommentByBoard(Board board); // 댓글수
}
