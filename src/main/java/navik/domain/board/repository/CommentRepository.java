package navik.domain.board.repository;

import java.util.List;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentCustomRepository {
    List<Comment> findByBoardAndIsDeletedFalse(Board board); // 지웠졌는지 확인
    Integer countCommentByBoard(Board board); // 댓글수
    @Query("SELECT c.board.id, COUNT(c) FROM Comment c where c.board.id IN :boardIds GROUP BY c.board.id")
    List<Object[]> countByBoardIds(@Param("boardIds") List<Long> boardIds);
}
