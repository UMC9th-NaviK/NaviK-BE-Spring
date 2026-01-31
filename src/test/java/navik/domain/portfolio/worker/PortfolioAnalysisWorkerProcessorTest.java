package navik.domain.portfolio.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.service.command.KpiScoreInitialService;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioAiDto;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioAnalysisWorkerProcessorTest {

	@Mock
	PortfolioRepository portfolioRepository;

	@Mock
	PortfolioAiClient portfolioAiClient;

	@Mock
	KpiScoreInitialService kpiScoreInitialService;

	@InjectMocks
	PortfolioAnalysisWorkerProcessor processor;

	@Nested
	@DisplayName("process()")
	class Process {

		@Test
		@DisplayName("성공 시 AI 분석 결과로 KPI 점수 초기화하고 true 반환")
		void success() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;
			String traceId = "trace-id";

			Portfolio portfolio = mockPortfolio("이력서 텍스트 내용");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));

			var scores = List.of(
				new PortfolioAiDto.AnalyzeResponse.KpiScoreItem(1L, "주력 언어 숙련도", 85, "high"),
				new PortfolioAiDto.AnalyzeResponse.KpiScoreItem(2L, "프레임워크 이해도", 70, "medium")
			);
			given(portfolioAiClient.analyzePortfolio("이력서 텍스트 내용"))
				.willReturn(new PortfolioAiDto.AnalyzeResponse(scores));

			// when
			boolean result = processor.process(userId, portfolioId, traceId);

			// then
			assertThat(result).isTrue();
			verify(kpiScoreInitialService).initializeKpiScores(eq(userId), any(KpiScoreRequestDTO.Initialize.class));
		}

		@Test
		@DisplayName("AI 응답이 KpiScoreRequestDTO로 정확히 변환된다")
		void aiResponse_mappedCorrectly() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio("이력서 내용");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));

			var scores = List.of(
				new PortfolioAiDto.AnalyzeResponse.KpiScoreItem(1L, "주력 언어 숙련도", 85, "high"),
				new PortfolioAiDto.AnalyzeResponse.KpiScoreItem(2L, "프레임워크 이해도", 70, "medium")
			);
			given(portfolioAiClient.analyzePortfolio("이력서 내용"))
				.willReturn(new PortfolioAiDto.AnalyzeResponse(scores));

			// when
			processor.process(userId, portfolioId, "trace-id");

			// then
			ArgumentCaptor<KpiScoreRequestDTO.Initialize> captor =
				ArgumentCaptor.forClass(KpiScoreRequestDTO.Initialize.class);
			verify(kpiScoreInitialService).initializeKpiScores(eq(userId), captor.capture());

			List<KpiScoreRequestDTO.Item> items = captor.getValue().scores();
			assertThat(items).hasSize(2);
			assertThat(items.get(0).kpiCardId()).isEqualTo(1L);
			assertThat(items.get(0).score()).isEqualTo(85);
			assertThat(items.get(1).kpiCardId()).isEqualTo(2L);
			assertThat(items.get(1).score()).isEqualTo(70);
		}

		@Test
		@DisplayName("포트폴리오가 존재하지 않으면 false 반환")
		void notFound() {
			// given
			Long userId = 1L;
			Long portfolioId = 999L;

			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.empty());

			// when
			boolean result = processor.process(userId, portfolioId, "trace-id");

			// then
			assertThat(result).isFalse();
			verify(portfolioAiClient, never()).analyzePortfolio(anyString());
			verify(kpiScoreInitialService, never()).initializeKpiScores(anyLong(), any());
		}

		@Test
		@DisplayName("content가 null이면 false 반환")
		void nullContent() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio(null);
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));

			// when
			boolean result = processor.process(userId, portfolioId, "trace-id");

			// then
			assertThat(result).isFalse();
			verify(portfolioAiClient, never()).analyzePortfolio(anyString());
		}

		@Test
		@DisplayName("content가 빈 문자열이면 false 반환")
		void emptyContent() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio("   ");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));

			// when
			boolean result = processor.process(userId, portfolioId, "trace-id");

			// then
			assertThat(result).isFalse();
			verify(portfolioAiClient, never()).analyzePortfolio(anyString());
		}

		@Test
		@DisplayName("AI 응답이 null이면 false 반환")
		void aiResponseNull() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio("이력서 내용");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
			given(portfolioAiClient.analyzePortfolio("이력서 내용")).willReturn(null);

			// when
			boolean result = processor.process(userId, portfolioId, "trace-id");

			// then
			assertThat(result).isFalse();
			verify(kpiScoreInitialService, never()).initializeKpiScores(anyLong(), any());
		}

		@Test
		@DisplayName("AI 응답의 scores가 비어있으면 false 반환")
		void aiResponseEmptyScores() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio("이력서 내용");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
			given(portfolioAiClient.analyzePortfolio("이력서 내용"))
				.willReturn(new PortfolioAiDto.AnalyzeResponse(List.of()));

			// when
			boolean result = processor.process(userId, portfolioId, "trace-id");

			// then
			assertThat(result).isFalse();
			verify(kpiScoreInitialService, never()).initializeKpiScores(anyLong(), any());
		}

		@Test
		@DisplayName("AI 호출 중 예외 발생 시 전파된다")
		void aiClientThrows() {
			// given
			Long userId = 1L;
			Long portfolioId = 10L;

			Portfolio portfolio = mockPortfolio("이력서 내용");
			given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
			given(portfolioAiClient.analyzePortfolio("이력서 내용"))
				.willThrow(new RuntimeException("AI 서버 에러"));

			// when & then
			assertThatThrownBy(() -> processor.process(userId, portfolioId, "trace-id"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("AI 서버 에러");
		}
	}

	// -------------------------
	// Helpers
	// -------------------------

	private Portfolio mockPortfolio(String content) {
		Portfolio portfolio = mock(Portfolio.class);
		lenient().when(portfolio.getContent()).thenReturn(content);
		return portfolio;
	}
}
