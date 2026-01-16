package navik.domain.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.recruitment.entity.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentCustomRepository {
}
