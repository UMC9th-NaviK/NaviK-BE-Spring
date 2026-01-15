package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardCustomRepository {
    List<Board> findAllByCursor(Long lastId, int pageSize);
    List<Board> findByJobAndCursor(String jobName, Long lastId, int pageSize);
    List<Board> findHotBoardsByCursor(Integer lastScore, Long lastId, int pageSize);
}
