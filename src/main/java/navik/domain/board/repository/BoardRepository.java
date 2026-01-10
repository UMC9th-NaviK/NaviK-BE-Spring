package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByArticleDeletedFalseOrderByCreatedAtDesc();
}
