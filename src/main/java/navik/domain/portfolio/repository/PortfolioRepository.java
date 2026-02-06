package navik.domain.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.portfolio.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	Optional<Portfolio> findTopByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Portfolio> findByIdAndUserId(Long id, Long userId);

}
