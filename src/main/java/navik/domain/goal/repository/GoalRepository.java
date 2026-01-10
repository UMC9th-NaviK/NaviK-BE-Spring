package navik.domain.goal.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.goal.entity.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {
	Slice<Goal> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, PageRequest pageRequest);

	Slice<Goal> findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(Long userId, Long cursor, PageRequest pageRequest);

	Slice<Goal> findByUserIdOrderByEndDateAscIdAsc(Long userId, PageRequest pageRequest);

	Slice<Goal> findByUserIdAndIdLessThanOrderByEndDateAscIdAsc(Long userId, Long cursor, PageRequest pageRequest);
}
