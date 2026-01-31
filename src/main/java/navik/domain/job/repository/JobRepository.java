package navik.domain.job.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import navik.domain.job.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
	List<Job> findByNameIn(Collection<String> names);

	Optional<Job> findByName(String name);
}
