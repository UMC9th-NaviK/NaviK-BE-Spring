package navik.domain.board.repository;

import navik.domain.board.entity.Board;
import navik.domain.job.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 전체 조회
    Page<Board> findAll(Pageable pageable);

    // 직무별 조회
    Page<Board> findByJobType(Pageable pageable, JobType jobType);
}
