package navik.domain.portfolio.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.event.PortfolioAnalysisEvent;
import navik.domain.portfolio.exception.code.PortfolioErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.portfolio.service.extractor.resolver.PortfolioTextExtractorResolver;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;
	private final UserQueryService userQueryService;
	private final PortfolioTextExtractorResolver portfolioTextExtractorResolver;
	private final ApplicationEventPublisher eventPublisher;

	public PortfolioResponseDto.Created createPortfolio(Long userId, PortfolioRequestDto.Create request) {
		User user = userQueryService.getUser(userId);

		String content = portfolioTextExtractorResolver.resolve(request.inputType())
			.extractText(request);

		Portfolio portfolio = Portfolio.builder()
			.inputType(request.inputType())
			.content(content)
			.fileUrl(request.fileUrl())
			.user(user)
			.build();

		portfolioRepository.save(portfolio);

		eventPublisher.publishEvent(new PortfolioAnalysisEvent(userId, portfolio.getId()));

		return new PortfolioResponseDto.Created(portfolio.getId(), request.inputType());
	}

	public PortfolioResponseDto.AdditionalInfoSubmitted submitAdditionalInfo(
		Long userId,
		Long portfolioId,
		PortfolioRequestDto.AdditionalInfo request
	) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new GeneralExceptionHandler(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

		if (!portfolio.getUser().getId().equals(userId)) {
			throw new GeneralExceptionHandler(PortfolioErrorCode.PORTFOLIO_NOT_OWNED);
		}

		if (portfolio.getStatus() != PortfolioStatus.FAILED) {
			throw new GeneralExceptionHandler(PortfolioErrorCode.INVALID_PORTFOLIO_STATUS);
		}

		portfolio.updateAdditionalInfo(
			request.qB1(),
			request.qB2(),
			request.qB3(),
			request.qB4(),
			request.qB5()
		);
		portfolioRepository.save(portfolio);

		eventPublisher.publishEvent(new PortfolioAnalysisEvent(userId, portfolioId));

		return new PortfolioResponseDto.AdditionalInfoSubmitted(portfolioId);
	}
}
