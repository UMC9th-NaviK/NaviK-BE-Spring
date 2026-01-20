package navik.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.study.entity.StudyUser;

public interface StudyUserRepository extends JpaRepository<StudyUser, Long> {
}
