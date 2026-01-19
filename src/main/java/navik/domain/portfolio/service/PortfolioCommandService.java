package navik.domain.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;
	private final UserQueryService userQueryService;

	public PortfolioResponseDto.Created createPortfolio(Long userId, PortfolioRequestDto.Create request) {
		User user = userQueryService.getUser(userId);

		PortfolioStatus status = switch (request.inputType()) {
			case PDF -> PortfolioStatus.PDF_PENDING;
			case TEXT -> PortfolioStatus.TEXT_INPUT;
		};

		Portfolio portfolio = Portfolio.builder()
			.inputType(request.inputType())
			.content(request.content())
			.fileUrl(request.fileUrl())
			.status(status)
			.user(user)
			.build();

		portfolioRepository.save(portfolio);

		return new PortfolioResponseDto.Created(userId, request.inputType());
	}
}
