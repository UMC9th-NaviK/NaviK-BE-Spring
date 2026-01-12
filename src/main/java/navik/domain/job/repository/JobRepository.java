package navik.domain.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.job.entity.Job;

public interface JobRepository extends JpaRepository<Long, Job> {
}
