package navik.domain.recruitment.repository.recruitment;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.recruitment.entity.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentCustomRepository {
	boolean existsByPostId(String postId);
}
