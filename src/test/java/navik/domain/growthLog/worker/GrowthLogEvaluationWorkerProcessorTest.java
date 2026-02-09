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
import navik.domain.job.entity.Job;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GrowthLogEvaluationWorkerProcessorTest {

	@Mock
	GrowthLogRepository growthLogRepository;

	@Mock
	GrowthLogEvaluationCoreService core;

	@Mock
	GrowthLogPersistenceService persistence;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	GrowthLogEvaluationWorkerProcessor processor;

	// -------------------------
	// 공통 상수
	// -------------------------
	private static final Long USER_ID = 1L;
	private static final Long JOB_ID = 50L;
	private static final Integer USER_LEVEL = 3;

	@Nested
	@DisplayName("process()")
	class Process {

		@Test
		@DisplayName("성공 시 COMPLETED 반환하고 persistence 호출")
		void success() {
			// given
			Long growthLogId = 10L;
			String traceId = "trace-id";
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "입력 내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(USER_ID, growthLogId, token))
				.willReturn(1);

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목",
				"내용",
				List.of(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3)),
				List.of()
			);
			var evaluated = new Evaluated(normalized, 3);

			given(core.buildContext(eq(USER_ID), eq("입력 내용"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, traceId, token);

			// then
			assertThat(result).isEqualTo(ProcessResult.COMPLETED);

			verify(persistence).completeGrowthLogAfterProcessing(
				eq(USER_ID), eq(growthLogId), eq(normalized), eq(3)
			);
			verify(growthLogRepository).clearProcessingTokenIfMatch(
				USER_ID, growthLogId, token, GrowthLogStatus.COMPLETED
			);
		}

		@Test
		@DisplayName("빈 content는 '(내용 없음)'으로 변환")
		void emptyContent_convertedToDefault() {
			// given
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "  ");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(USER_ID, growthLogId, token))
				.willReturn(1);

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목", "내용", List.of(), List.of()
			);
			var evaluated = new Evaluated(normalized, 0);

			given(core.buildContext(eq(USER_ID), eq("(내용 없음)"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);

			// when
			processor.process(USER_ID, growthLogId, "trace-id", token);

			// then
			verify(core).buildContext(USER_ID, "(내용 없음)");
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 SKIP_NOT_FOUND 반환")
		void notFound() {
			// given
			Long growthLogId = 999L;

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.empty());

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_FOUND);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
			verify(persistence, never()).completeGrowthLogAfterProcessing(anyLong(), anyLong(), any(), anyInt());
		}

		@Test
		@DisplayName("이미 COMPLETED 상태면 SKIP_ALREADY_COMPLETED 반환")
		void alreadyCompleted() {
			// given
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.COMPLETED, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_ALREADY_COMPLETED);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
		}

		@Test
		@DisplayName("PROCESSING이 아닌 상태(PENDING/FAILED)면 SKIP_NOT_PROCESSING 반환")
		void notProcessing_pending() {
			// given
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PENDING, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_PROCESSING);
		}

		@Test
		@DisplayName("PROCESSING이 아닌 상태(FAILED)면 SKIP_NOT_PROCESSING 반환")
		void notProcessing_failed() {
			// given
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.FAILED, null, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", "token");

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_NOT_PROCESSING);
		}

		@Test
		@DisplayName("토큰 불일치면 SKIP_TOKEN_MISMATCH 반환")
		void tokenMismatch() {
			// given
			Long growthLogId = 10L;
			String existingToken = "existing-token";
			String newToken = "new-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, existingToken, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", newToken);

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_TOKEN_MISMATCH);

			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
		}

		@Test
		@DisplayName("Lock 획득 실패면 SKIP_ALREADY_APPLYING 반환")
		void lockFailed() {
			// given
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(USER_ID, growthLogId, token))
				.willReturn(0);

			// when
			ProcessResult result = processor.process(USER_ID, growthLogId, "trace-id", token);

			// then
			assertThat(result).isEqualTo(ProcessResult.SKIP_ALREADY_APPLYING);

			verify(core, never()).buildContext(anyLong(), anyString());
			verify(persistence, never()).completeGrowthLogAfterProcessing(anyLong(), anyLong(), any(), anyInt());
		}

		@Test
		@DisplayName("여러 KPI delta 합산이 정확하다")
		void multipleKpiDeltas_sumCorrectly() {
			// given
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(USER_ID, growthLogId, token))
				.willReturn(1);

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var kpis = List.of(
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3),
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(101L, 5),
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(102L, -2)
			);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", kpis, List.of());
			var evaluated = new Evaluated(normalized, 6); // 3 + 5 + (-2) = 6

			given(core.buildContext(eq(USER_ID), anyString())).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);

			// when
			processor.process(USER_ID, growthLogId, "trace-id", token);

			// then
			verify(persistence).completeGrowthLogAfterProcessing(
				eq(USER_ID), eq(growthLogId), eq(normalized), eq(6)
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
			Long growthLogId = 10L;
			String token = "test-token";

			// when
			processor.markFailedIfProcessing(USER_ID, growthLogId, token);

			// then
			verify(growthLogRepository).updateStatusIfMatchAndToken(
				USER_ID, growthLogId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED, token
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

	private void mockUser(Long userId, Integer level, Long jobId) {
		User user = mock(User.class);
		lenient().when(user.getLevel()).thenReturn(level);

		if (jobId != null) {
			var job = mock(Job.class);
			lenient().when(job.getId()).thenReturn(jobId);
			lenient().when(user.getJob()).thenReturn(job);
		} else {
			lenient().when(user.getJob()).thenReturn(null);
		}

		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));
	}
}