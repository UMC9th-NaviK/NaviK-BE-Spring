package navik.domain.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.dto.PortfolioResponseDTO;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.exception.code.PortfolioErrorCode;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioQueryService {
	private final PortfolioRepository portfolioRepository;

	public PortfolioResponseDTO.Status getPortfolioStatus(Long userId, Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new GeneralException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

		if (!portfolio.getUser().getId().equals(userId)) {
			throw new GeneralException(PortfolioErrorCode.PORTFOLIO_NOT_OWNED);
		}

		return new PortfolioResponseDTO.Status(portfolioId, portfolio.getStatus());
	}
}
