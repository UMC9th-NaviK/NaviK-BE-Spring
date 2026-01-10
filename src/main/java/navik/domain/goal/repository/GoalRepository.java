package navik.domain.goal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.goal.entity.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
