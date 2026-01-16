package navik.domain.board.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import navik.domain.board.entity.Board;

@Repository
public interface BoardCustomRepository {
	List<Board> findAllByCursor(Long lastId, int pageSize);

	List<Board> findByJobAndCursor(String jobName, Long lastId, int pageSize);

	List<Board> findHotBoardsByCursor(Integer lastScore, Long lastId, int pageSize);
}
