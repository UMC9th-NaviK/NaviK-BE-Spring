package navik.domain.growthLog.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.growthLog.dto.internal.Evaluated;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.ProcessResult;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;

@ExtendWith(MockitoExtension.class)
class GrowthLogEvaluationWorkerProcessorTest {

	@Mock
	GrowthLogRepository growthLogRepository;

	@Mock
	GrowthLogEvaluationCoreService core;

	@Mock
	GrowthLogPersistenceService persistence;

	@InjectMocks
	GrowthLogEvaluationWorkerProcessor processor;

	@Nested
	@DisplayName("process()")
	class Process {

		@Test
		@DisplayName("성공 시 COMPLETED 반환하고 persistence 호출")
		void success() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String traceId = "trace-id";
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "입력 내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목", "내용",
				List.of(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3))
			);
			var evaluated = new Evaluated(normalized, normalized.kpis(), 3);

			given(core.buildContext(eq(userId), eq("입력 내용"))).willReturn(context);
			given(core.evaluate(eq(userId), eq(context))).willReturn(evaluated);

			// when
			ProcessResult result = processor.process(userId, growthLogId, traceId, token);

			// then
			assertThat(result).isEqualTo(ProcessResult.COMPLETED);

			verify(persistence).completeGrowthLogAfterProcessing(
				eq(userId), eq(growthLogId), eq(normalized), eq(3), any()
			);
			verify(growthLogRepository).clearProcessingTokenIfMatch(
				userId, growthLogId, token, GrowthLogStatus.COMPLETED
			);
		}

		@Test
		@DisplayName("빈 content는 '(내용 없음)'으로 변환")
		void emptyContent_convertedToDefault() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "  ");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", List.of());
			var evaluated = new Evaluated(normalized, List.of(), 0);

			given(core.buildContext(eq(userId), eq("(내용 없음)"))).willReturn(context);
			given(core.evaluate(eq(userId), eq(context))).willReturn(evaluated);

			// when
			processor.process(userId, growthLogId, "trace-id", token);

			// then
			verify(core).buildContext(userId, "(내용 없음)");
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 SKIP_NOT_FOUND 반환")
		void notFound() {
			// given
			Long userId = 1L;
			Long growthLogId = 999L;

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.empty());

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_FOUND);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
			verify(persistence, never()).completeGrowthLogAfterProcessing(
				anyLong(), anyLong(), any(), anyInt(), any()
			);
		}

		@Test
		@DisplayName("이미 COMPLETED 상태면 SKIP_ALREADY_COMPLETED 반환")
		void alreadyCompleted() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.COMPLETED, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_ALREADY_COMPLETED);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
		}

		@Test
		@DisplayName("PROCESSING이 아닌 상태(PENDING/FAILED)면 SKIP_NOT_PROCESSING 반환")
		void notProcessing_pending() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PENDING, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_PROCESSING);
		}

		@Test
		@DisplayName("PROCESSING이 아닌 상태(FAILED)면 SKIP_NOT_PROCESSING 반환")
		void notProcessing_failed() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.FAILED, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_PROCESSING);
		}

		@Test
		@DisplayName("토큰 불일치면 SKIP_TOKEN_MISMATCH 반환")
		void tokenMismatch() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String existingToken = "existing-token";
			String newToken = "new-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, existingToken, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", newToken);

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_TOKEN_MISMATCH);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
		}

		@Test
		@DisplayName("Lock 획득 실패면 SKIP_ALREADY_APPLYING 반환")
		void lockFailed() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(0);

			// when
			ProcessResult result = processor.process(userId, growthLogId, "trace-id", token);

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_ALREADY_APPLYING);

			verify(core, never()).buildContext(anyLong(), anyString());
			verify(persistence, never()).completeGrowthLogAfterProcessing(
				anyLong(), anyLong(), any(), anyInt(), any()
			);
		}

		@Test
		@DisplayName("여러 KPI delta 합산이 정확하다")
		void multipleKpiDeltas_sumCorrectly() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var kpis = List.of(
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3),
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(101L, 5),
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(102L, -2)
			);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", kpis);
			var evaluated = new Evaluated(normalized, kpis, 6);  // 3 + 5 + (-2) = 6

			given(core.buildContext(eq(userId), anyString())).willReturn(context);
			given(core.evaluate(eq(userId), eq(context))).willReturn(evaluated);

			// when
			processor.process(userId, growthLogId, "trace-id", token);

			// then
			verify(persistence).completeGrowthLogAfterProcessing(
				eq(userId), eq(growthLogId), eq(normalized), eq(6), any()
			);
		}
	}

	@Nested
	@DisplayName("markFailedIfProcessing()")
	class MarkFailedIfProcessing {

		@Test
		@DisplayName("토큰이 일치하면 PROCESSING → FAILED로 변경")
		void success() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			// when
			processor.markFailedIfProcessing(userId, growthLogId, token);

			// then
			verify(growthLogRepository).updateStatusIfMatchAndToken(
				userId, growthLogId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED, token
			);
		}
	}

	// -------------------------
	// Helpers
	// -------------------------

	private GrowthLog mockGrowthLog(GrowthLogStatus status, String processingToken, String content) {
		GrowthLog growthLog = mock(GrowthLog.class);
		lenient().when(growthLog.getStatus()).thenReturn(status);
		lenient().when(growthLog.getProcessingToken()).thenReturn(processingToken);
		lenient().when(growthLog.getContent()).thenReturn(content);
		return growthLog;
	}
}