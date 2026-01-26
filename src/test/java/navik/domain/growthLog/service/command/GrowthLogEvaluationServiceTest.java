package navik.domain.growthLog.service.command;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.growthLog.ai.client.GrowthLogAiClient;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogKpiLinkRepository;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.repository.PortfolioRepository;

@ExtendWith(MockitoExtension.class)
class GrowthLogEvaluationServiceTest {

	@Mock
	GrowthLogRepository growthLogRepository;
	@Mock
	GrowthLogKpiLinkRepository growthLogKpiLinkRepository;
	@Mock
	KpiCardRepository kpiCardRepository;
	@Mock
	PortfolioRepository portfolioRepository;

	@Mock
	GrowthLogAiClient growthLogAiClient;
	@Mock
	GrowthLogPersistenceService growthLogPersistenceService;
	@Mock
	RetryRateLimiter retryRateLimiter;

	@InjectMocks
	GrowthLogEvaluationService service;

	@Test
	void create_성공_시_context_구성되고_completed로_저장된다() {
		// given
		Long userId = 1L;

		givenLatestPortfolio(userId, "포트폴리오 제목", "포트폴리오 내용");

		GrowthLog recent = mockLog(10L, GrowthType.PORTFOLIO, "지난 로그", "지난 내용",
			LocalDateTime.of(2026, 1, 1, 0, 0));
		givenRecentLogs(userId, List.of(recent));

		givenLinksFor(List.of(10L), List.of(mockLink(recent, 100L, 2)));

		givenAiReturns(
			new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목",
				"내용",
				List.of(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3))
			)
		);

		given(kpiCardRepository.countByIdIn(List.of(100L))).willReturn(1L);
		given(growthLogPersistenceService.saveUserInputLog(eq(userId), any(), eq(3), any()))
			.willReturn(999L);

		// when
		GrowthLogResponseDTO.CreateResult result =
			service.create(userId, new GrowthLogRequestDTO.CreateUserInput("입력"));

		// then - 결과
		assertThat(result.id()).isEqualTo(999L);
		assertThat(result.status()).isEqualTo(GrowthLogStatus.COMPLETED);

		// then - AI로 전달된 context 검증 (핵심만)
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext ctx = captureContext(userId);
		assertThat(ctx.resumeText()).contains("포트폴리오 제목");
		assertThat(ctx.resumeText()).contains("포트폴리오 내용");
		assertThat(ctx.newContent()).isEqualTo("입력");
		assertThat(ctx.recentGrowthLogs().size()).isEqualTo(1);
		assertThat(ctx.recentGrowthLogs().get(0).id()).isEqualTo(10L);
		assertThat(ctx.recentKpiDeltas().size()).isEqualTo(1);
		assertThat(ctx.recentKpiDeltas().get(0).kpiCardId()).isEqualTo(100L);

		// then - 동작 검증
		verify(growthLogPersistenceService, never()).saveFailedUserInputLog(anyLong(), anyString());
		verify(growthLogKpiLinkRepository).findByGrowthLogIdIn(List.of(10L));
		verify(kpiCardRepository).countByIdIn(List.of(100L));
	}

	@Test
	void create_시_AI예외면_failed로_저장하고_completed저장은_호출되지_않는다() {
		// given
		Long userId = 1L;

		givenNoPortfolio(userId);
		givenRecentLogs(userId, List.of());

		given(growthLogAiClient.evaluateUserInput(anyLong(), any()))
			.willThrow(new RuntimeException("AI down"));

		given(growthLogPersistenceService.saveFailedUserInputLog(eq(userId), anyString()))
			.willReturn(123L);

		// when
		GrowthLogResponseDTO.CreateResult result =
			service.create(userId, new GrowthLogRequestDTO.CreateUserInput("입력"));

		// then
		assertThat(result.status()).isEqualTo(GrowthLogStatus.FAILED);
		assertThat(result.id()).isEqualTo(123L);

		verify(growthLogPersistenceService, never())
			.saveUserInputLog(anyLong(), any(), anyInt(), any());

		// 구현상 recentLogs 비어있으면 link 조회도 안 함
		verify(growthLogKpiLinkRepository, never()).findByGrowthLogIdIn(anyList());
	}

	@Test
	void create_시_최근_성장로그_여러개가_context로_전달되고_recentIds로_link조회한다() {
		// given
		Long userId = 1L;

		givenLatestPortfolio(userId, "포트폴리오 제목", "포트폴리오 내용");

		GrowthLog log1 = mockLog(10L, GrowthType.PORTFOLIO, "로그1", "내용1",
			LocalDateTime.of(2026, 1, 1, 0, 0));
		GrowthLog log2 = mockLog(11L, GrowthType.USER_INPUT, "로그2", "내용2",
			LocalDateTime.of(2026, 1, 2, 0, 0));
		GrowthLog log3 = mockLog(12L, GrowthType.PORTFOLIO, "로그3", "내용3",
			LocalDateTime.of(2026, 1, 3, 0, 0));

		givenRecentLogs(userId, List.of(log1, log2, log3));

		givenLinksFor(
			List.of(10L, 11L, 12L),
			List.of(
				mockLink(log1, 100L, 1),
				mockLink(log2, 101L, 2)
			)
		);

		givenAiReturns(
			new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목",
				"내용",
				List.of(
					new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 1),
					new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(101L, 2)
				)
			)
		);

		given(kpiCardRepository.countByIdIn(List.of(100L, 101L))).willReturn(2L);
		given(growthLogPersistenceService.saveUserInputLog(eq(userId), any(), eq(3), any()))
			.willReturn(999L);

		// when
		GrowthLogResponseDTO.CreateResult result =
			service.create(userId, new GrowthLogRequestDTO.CreateUserInput("입력"));

		// then - 결과
		assertThat(result.status()).isEqualTo(GrowthLogStatus.COMPLETED);
		assertThat(result.id()).isEqualTo(999L);

		// then - AI로 전달된 context (여러 개)
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext ctx = captureContext(userId);
		assertThat(ctx.recentGrowthLogs().size()).isEqualTo(3);

		List<Long> ids = ctx.recentGrowthLogs().stream()
			.map(GrowthLogAiRequestDTO.PastGrowthLog::id)
			.toList();
		assertThat(ids).isEqualTo(List.of(10L, 11L, 12L)); // findTop20 결과 순서 그대로 매핑됨

		// then - link 조회가 recentIds로 갔는지
		verify(growthLogKpiLinkRepository).findByGrowthLogIdIn(List.of(10L, 11L, 12L));
		verify(kpiCardRepository).countByIdIn(List.of(100L, 101L));
		verify(growthLogPersistenceService, never()).saveFailedUserInputLog(anyLong(), anyString());
	}

	// -------------------------
	// Helpers
	// -------------------------

	private void givenLatestPortfolio(Long userId, String title, String content) {
		Portfolio p = Portfolio.builder().title(title).content(content).build();
		given(portfolioRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(Optional.of(p));
	}

	private void givenNoPortfolio(Long userId) {
		given(portfolioRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(Optional.empty());
	}

	private GrowthLog mockLog(Long id, GrowthType type, String title, String content, LocalDateTime createdAt) {
		GrowthLog gl = mock(GrowthLog.class);
		given(gl.getId()).willReturn(id);
		given(gl.getType()).willReturn(type);
		given(gl.getTitle()).willReturn(title);
		given(gl.getContent()).willReturn(content);
		given(gl.getTotalDelta()).willReturn(0);
		given(gl.getCreatedAt()).willReturn(createdAt);
		return gl;
	}

	private void givenRecentLogs(Long userId, List<GrowthLog> logs) {
		given(growthLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(logs);
	}

	private GrowthLogKpiLink mockLink(GrowthLog growthLog, Long kpiId, int delta) {
		KpiCard kpiCard = mock(KpiCard.class);
		given(kpiCard.getId()).willReturn(kpiId);

		GrowthLogKpiLink link = mock(GrowthLogKpiLink.class);
		given(link.getGrowthLog()).willReturn(growthLog);
		given(link.getKpiCard()).willReturn(kpiCard);
		given(link.getDelta()).willReturn(delta);
		return link;
	}

	private void givenLinksFor(List<Long> growthLogIds, List<GrowthLogKpiLink> links) {
		given(growthLogKpiLinkRepository.findByGrowthLogIdIn(growthLogIds))
			.willReturn(links);
	}

	private void givenAiReturns(GrowthLogAiResponseDTO.GrowthLogEvaluationResult aiResult) {
		given(growthLogAiClient.evaluateUserInput(anyLong(), any()))
			.willReturn(aiResult);
	}

	private GrowthLogAiRequestDTO.GrowthLogEvaluationContext captureContext(Long userId) {
		ArgumentCaptor<GrowthLogAiRequestDTO.GrowthLogEvaluationContext> captor =
			ArgumentCaptor.forClass(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);

		verify(growthLogAiClient).evaluateUserInput(eq(userId), captor.capture());
		return captor.getValue();
	}
}
