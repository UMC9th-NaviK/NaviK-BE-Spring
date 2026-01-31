package navik.domain.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.portfolio.service.extractor.resolver.PortfolioTextExtractorResolver;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCommandService {

	private final PortfolioRepository portfolioRepository;
	private final UserQueryService userQueryService;
	private final PortfolioAiClient portfolioAiClient;
	private final PortfolioTextExtractorResolver portfolioTextExtractorResolver;

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

		return new PortfolioResponseDto.Created(portfolio.getId(), request.inputType());
	}
}
