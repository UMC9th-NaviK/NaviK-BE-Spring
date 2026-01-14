package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import navik.domain.board.entity.BoardLike;
import navik.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByBoardAndUser(Board board, User user); // 삭제를 위해 엔티티를 직접 조회하는 메서드
    long countLikeByBoard(Board board); // 게시글 좋아요 총합
}
