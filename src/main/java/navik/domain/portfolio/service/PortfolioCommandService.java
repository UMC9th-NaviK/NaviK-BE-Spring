package navik.domain.portfolio.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.exception.code.PortfolioErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.domain.portfolio.message.PortfolioAnalysisMessage;
import navik.domain.portfolio.message.PortfolioAnalysisPublisher;
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
	private final PortfolioAnalysisPublisher portfolioAnalysisPublisher;

	public PortfolioResponseDto.Created createPortfolio(Long userId, PortfolioRequestDto.Create request) {
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

		publishAnalysisMessage(userId, portfolio.getId());

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

		publishAnalysisMessage(userId, portfolioId);

		return new PortfolioResponseDto.AdditionalInfoSubmitted(portfolioId);
	}

	private void publishAnalysisMessage(Long userId, Long portfolioId) {
		try {
			String traceId = UUID.randomUUID().toString();
			portfolioAnalysisPublisher.publish(new PortfolioAnalysisMessage(userId, portfolioId, traceId));
		} catch (Exception e) {
			log.error("[PortfolioCommandService] 분석 메시지 발행 실패. userId={}, portfolioId={}", userId, portfolioId, e);
		}
	}
}
