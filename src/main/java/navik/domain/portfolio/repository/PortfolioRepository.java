package navik.domain.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.portfolio.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
