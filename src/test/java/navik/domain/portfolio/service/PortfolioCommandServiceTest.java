package navik.domain.portfolio.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.domain.portfolio.entity.InputType;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.message.PortfolioAnalysisMessage;
import navik.domain.portfolio.message.PortfolioAnalysisPublisher;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.portfolio.service.extractor.resolver.PortfolioTextExtractorResolver;
import navik.domain.portfolio.service.extractor.strategy.PortfolioTextExtractor;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@ExtendWith(MockitoExtension.class)
class PortfolioCommandServiceTest {

	@Mock
	PortfolioRepository portfolioRepository;

	@Mock
	UserQueryService userQueryService;

	@Mock
	PortfolioTextExtractorResolver portfolioTextExtractorResolver;

	@Mock
	PortfolioAnalysisPublisher portfolioAnalysisPublisher;

	@InjectMocks
	PortfolioCommandService portfolioCommandService;

	@Nested
	@DisplayName("createPortfolio()")
	class CreatePortfolio {

		@Test
		@DisplayName("포트폴리오 저장 후 분석 메시지를 발행한다")
		void success_publishesAnalysisMessage() {
			// given
			Long userId = 1L;
			var request = new PortfolioRequestDto.Create(InputType.TEXT, "이력서 내용", null);

			User user = mock(User.class);
			given(userQueryService.getUser(userId)).willReturn(user);

			PortfolioTextExtractor extractor = mock(PortfolioTextExtractor.class);
			given(portfolioTextExtractorResolver.resolve(InputType.TEXT)).willReturn(extractor);
			given(extractor.extractText(request)).willReturn("이력서 내용");

			given(portfolioRepository.save(any(Portfolio.class))).willAnswer(invocation -> {
				Portfolio p = invocation.getArgument(0);
				ReflectionTestUtils.setField(p, "id", 10L);
				return p;
			});

			// when
			PortfolioResponseDto.Created result = portfolioCommandService.createPortfolio(userId, request);

			// then
			assertThat(result.id()).isEqualTo(10L);
			assertThat(result.inputType()).isEqualTo(InputType.TEXT);

			verify(portfolioRepository).save(any(Portfolio.class));
			verify(portfolioAnalysisPublisher).publish(any(PortfolioAnalysisMessage.class));
		}

		@Test
		@DisplayName("분석 메시지 발행 실패해도 포트폴리오는 정상 저장된다")
		void publishFails_portfolioStillSaved() {
			// given
			Long userId = 1L;
			var request = new PortfolioRequestDto.Create(InputType.TEXT, "이력서 내용", null);

			User user = mock(User.class);
			given(userQueryService.getUser(userId)).willReturn(user);

			PortfolioTextExtractor extractor = mock(PortfolioTextExtractor.class);
			given(portfolioTextExtractorResolver.resolve(InputType.TEXT)).willReturn(extractor);
			given(extractor.extractText(request)).willReturn("이력서 내용");

			given(portfolioRepository.save(any(Portfolio.class))).willAnswer(invocation -> {
				Portfolio p = invocation.getArgument(0);
				ReflectionTestUtils.setField(p, "id", 10L);
				return p;
			});

			willThrow(new RuntimeException("Redis 연결 실패"))
				.given(portfolioAnalysisPublisher).publish(any(PortfolioAnalysisMessage.class));

			// when
			PortfolioResponseDto.Created result = portfolioCommandService.createPortfolio(userId, request);

			// then
			assertThat(result.id()).isEqualTo(10L);
			verify(portfolioRepository).save(any(Portfolio.class));
		}

		@Test
		@DisplayName("PDF 타입도 정상적으로 저장되고 분석 메시지가 발행된다")
		void pdfType_success() {
			// given
			Long userId = 1L;
			var request = new PortfolioRequestDto.Create(InputType.PDF, null, "https://s3.example.com/resume.pdf");

			User user = mock(User.class);
			given(userQueryService.getUser(userId)).willReturn(user);

			PortfolioTextExtractor extractor = mock(PortfolioTextExtractor.class);
			given(portfolioTextExtractorResolver.resolve(InputType.PDF)).willReturn(extractor);
			given(extractor.extractText(request)).willReturn("PDF에서 추출된 텍스트");

			given(portfolioRepository.save(any(Portfolio.class))).willAnswer(invocation -> {
				Portfolio p = invocation.getArgument(0);
				ReflectionTestUtils.setField(p, "id", 20L);
				return p;
			});

			// when
			PortfolioResponseDto.Created result = portfolioCommandService.createPortfolio(userId, request);

			// then
			assertThat(result.id()).isEqualTo(20L);
			assertThat(result.inputType()).isEqualTo(InputType.PDF);

			verify(portfolioAnalysisPublisher).publish(any(PortfolioAnalysisMessage.class));
		}
	}
}
