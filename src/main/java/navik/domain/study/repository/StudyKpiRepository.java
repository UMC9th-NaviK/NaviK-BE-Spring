package navik.domain.study.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.study.entity.StudyKpi;

public interface StudyKpiRepository extends JpaRepository<StudyKpi, Long> {
	// 스터디 ID 리스트에 해당하는 모든 StudyKpi 데이터 조회
	// 연관된 kpicard까지 한 번의 쿼리로 가져옴
	@EntityGraph(attributePaths = {"kpiCard", "study"})
	List<StudyKpi> findByStudyIdIn(List<Long> studyIds);
}
