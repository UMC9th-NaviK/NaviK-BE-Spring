package navik.domain.board.repository.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentCustomRepository {

	Integer countCommentByBoard(Board board); // 댓글수

	@Query("SELECT c.board.id, COUNT(c) FROM Comment c where c.board.id IN :boardIds AND c.isDeleted = false GROUP BY  c.board.id")
	List<Object[]> countByBoardIds(@Param("boardIds") List<Long> boardIds);
}
