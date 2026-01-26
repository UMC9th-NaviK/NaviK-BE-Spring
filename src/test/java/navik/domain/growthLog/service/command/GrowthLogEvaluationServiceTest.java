package navik.domain.growthLog.service.command;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.growthLog.ai.client.GrowthLogAiClient;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
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
	void create_시_포트폴리오와_최근로그가_context로_전달되고_성공이면_completed로_저장된다() {
		// given
		Long userId = 1L;

		Portfolio p = Portfolio.builder()
			.title("포트폴리오 제목")
			.content("포트폴리오 내용")
			.build();
		given(portfolioRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(Optional.of(p));

		// 최근 로그 1개: 서비스는 builder 필드 접근이 아니라 getter를 사용하므로 mock으로 세팅
		GrowthLog recent = mock(GrowthLog.class);
		given(recent.getId()).willReturn(10L);
		given(recent.getType()).willReturn(GrowthType.PORTFOLIO);
		given(recent.getTitle()).willReturn("지난 로그");
		given(recent.getContent()).willReturn("지난 내용");
		given(recent.getTotalDelta()).willReturn(2);
		given(recent.getCreatedAt()).willReturn(LocalDateTime.of(2026, 1, 1, 0, 0));

		given(growthLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(List.of(recent));

		// KPI 링크: GrowthLogKpiLink는 getter를 쓰므로 내부 객체들도 mock/getter 스텁이 안전
		KpiCard kpiCard = mock(KpiCard.class);
		given(kpiCard.getId()).willReturn(100L);

		GrowthLogKpiLink link = mock(GrowthLogKpiLink.class);
		given(link.getGrowthLog()).willReturn(recent);
		given(link.getKpiCard()).willReturn(kpiCard);
		given(link.getDelta()).willReturn(2);

		given(growthLogKpiLinkRepository.findByGrowthLogIdIn(List.of(10L)))
			.willReturn(List.of(link));

		// AI 응답 mock + context 검증
		given(growthLogAiClient.evaluateUserInput(
			eq(userId),
			argThat(ctx ->
				ctx.resumeText().contains("포트폴리오 제목")
					&& ctx.resumeText().contains("포트폴리오 내용")
					&& ctx.newContent().equals("입력")
					&& ctx.recentGrowthLogs().size() == 1
					&& ctx.recentGrowthLogs().get(0).id().equals(10L)
					&& ctx.recentKpiDeltas().size() == 1
					&& ctx.recentKpiDeltas().get(0).growthLogId().equals(10L)
					&& ctx.recentKpiDeltas().get(0).kpiCardId().equals(100L)
			)
		)).willReturn(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
			"제목", "내용",
			List.of(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3))
		));

		given(kpiCardRepository.countByIdIn(List.of(100L))).willReturn(1L);

		given(growthLogPersistenceService.saveUserInputLog(eq(userId), any(), eq(3), any()))
			.willReturn(999L);

		// when
		GrowthLogResponseDTO.CreateResult result =
			service.create(userId, new GrowthLogRequestDTO.CreateUserInput("입력"));

		// then
		assertThat(result.id()).isEqualTo(999L);
		assertThat(result.status()).isEqualTo(GrowthLogStatus.COMPLETED);

		verify(growthLogPersistenceService, never()).saveFailedUserInputLog(anyLong(), anyString());
		verify(growthLogKpiLinkRepository).findByGrowthLogIdIn(List.of(10L));
		verify(kpiCardRepository).countByIdIn(List.of(100L));
	}

	@Test
	void create_시_AI예외면_failed로_저장하고_completed저장은_호출되지_않는다() {
		// given
		Long userId = 1L;

		given(portfolioRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(Optional.empty());
		given(growthLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId))
			.willReturn(List.of());

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

		// 최근 로그가 비어있으면 KPI 링크 조회도 호출되지 않는 게 구현상 맞음
		verify(growthLogKpiLinkRepository, never()).findByGrowthLogIdIn(anyList());
	}

}
