package navik.domain.goal.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.goal.entity.Goal;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
	Slice<Goal> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, PageRequest pageRequest);

	Slice<Goal> findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(Long userId, Long cursor, PageRequest pageRequest);

	Slice<Goal> findByUserIdOrderByEndDateAscIdAsc(Long userId, PageRequest pageRequest);

	Slice<Goal> findByUserIdAndIdLessThanOrderByEndDateAscIdAsc(Long userId, Long cursor, PageRequest pageRequest);

    Optional<Object> findByIdAndUserId(Long userId, Long goalId);

    @Query("SELECT g FROM Goal g JOIN FETCH g.user WHERE g.endDate IN :endDates")
	List<Goal> findByEndDateIn(List<LocalDate> endDates);
}
