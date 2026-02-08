package navik.domain.portfolio.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.dto.PortfolioResponseDTO;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.event.PortfolioAnalysisEvent;
import navik.domain.portfolio.exception.code.PortfolioErrorCode;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.portfolio.service.extractor.resolver.PortfolioTextExtractorResolver;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;
	private final UserQueryService userQueryService;
	private final PortfolioTextExtractorResolver portfolioTextExtractorResolver;
	private final ApplicationEventPublisher eventPublisher;

	public PortfolioResponseDTO.Created createPortfolio(Long userId, PortfolioRequestDTO.Create request) {
		User user = userQueryService.getUser(userId);

		String content = portfolioTextExtractorResolver.resolve(request.inputType())
			.extractText(request); // 이력서는 반드시 텍스트로 변환

		Portfolio portfolio = Portfolio.builder()
			.inputType(request.inputType())
			.content(content)
			.fileUrl(request.fileUrl()) // 파일이 아닌경우 null
			.user(user)
			.build();

		portfolioRepository.save(portfolio);

		eventPublisher.publishEvent(new PortfolioAnalysisEvent(userId, portfolio.getId(),false));

		return new PortfolioResponseDTO.Created(portfolio.getId(), request.inputType(),portfolio.getStatus());
	}

	public PortfolioResponseDTO.AdditionalInfoSubmitted submitAdditionalInfo(
		Long userId,
		Long portfolioId,
		PortfolioRequestDTO.AdditionalInfo request
	) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new GeneralException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

		if (!portfolio.getUser().getId().equals(userId)) {
			throw new GeneralException(PortfolioErrorCode.PORTFOLIO_NOT_OWNED);
		}

		if (portfolio.getStatus() != PortfolioStatus.RETRY_REQUIRED) {
			throw new GeneralException(PortfolioErrorCode.INVALID_PORTFOLIO_STATUS);
		}

		portfolio.updateAdditionalInfo(
			request.qB1(),
			request.qB2(),
			request.qB3(),
			request.qB4(),
			request.qB5()
		);
		portfolioRepository.save(portfolio);

		eventPublisher.publishEvent(new PortfolioAnalysisEvent(userId, portfolioId, true));

		return new PortfolioResponseDTO.AdditionalInfoSubmitted(portfolioId);
	}
}
