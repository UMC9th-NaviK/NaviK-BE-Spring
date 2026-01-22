package navik.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.study.entity.StudyKpi;

public interface StudyKpiRepository extends JpaRepository<StudyKpi, Long> {
}
