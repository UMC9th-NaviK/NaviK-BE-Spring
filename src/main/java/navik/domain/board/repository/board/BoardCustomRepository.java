package navik.domain.board.repository.board;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import navik.domain.board.entity.Board;

@Repository
public interface BoardCustomRepository {
	List<Board> findAllByCursor(LocalDateTime lastCreatedAt, int pageSize);

	List<Board> findByJobAndCursor(String jobName, LocalDateTime lastCreatedAt, int pageSize);

	List<Board> findHotBoardsByCursor(Integer lastScore, LocalDateTime lastCreatedAt, int pageSize);

	List<Board> searchByKeyword(String keyword, String type, String jobName, Integer lastScore,
		LocalDateTime lastCreatedAt, int size);
}
