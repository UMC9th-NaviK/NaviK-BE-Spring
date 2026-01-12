package navik.domain.job.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.job.entity.Job;
import navik.domain.job.enums.JobType;

public interface JobRepository extends JpaRepository<Job, Long> {
	Optional<Job> findByJobType(JobType jobType);
}
