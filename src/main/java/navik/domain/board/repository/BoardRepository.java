package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 전체 조회
    Page<Board> findAll(Pageable pageable);

    // 직무별 조회
    @Query("select b from Board b where b.user.job.name = :jobName")
    Page<Board> findByUserJobType(@Param("jobName") String jobType, Pageable pageable);
}
