package navik.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {
}
