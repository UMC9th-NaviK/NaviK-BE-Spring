package navik.domain.board.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardCustomRepository {
	// 좋아요 수 +1
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Board b SET b.articleLikes = b.articleLikes + 1 WHERE b.id = :id")
	void incrementArticleLikes(@Param("id") Long id);

	// 좋아요 수 -1
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Board b SET b.articleLikes = b.articleLikes - 1 WHERE b.id = :id AND b.articleLikes > 0")
	void decrementArticleLikes(@Param("id") Long id);

	// 조회수 확인
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Board b SET b.articleViews = b.articleViews + 1 WHERE b.id = :id")
	int incrementArticleViews(@Param("id") Long id);
}
