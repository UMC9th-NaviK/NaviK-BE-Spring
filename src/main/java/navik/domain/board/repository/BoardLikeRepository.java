package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import navik.domain.board.entity.BoardLike;
import navik.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    boolean existsByBoardAndUser(Board board, User user); // 사용자가 이미 좋아요 눌렀는지 확인용
    long countLikeByBoard(Board board); // 게시글 좋아요 총합
}
