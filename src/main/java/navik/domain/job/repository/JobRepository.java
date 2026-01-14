package navik.domain.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.job.entity.Job;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
}
