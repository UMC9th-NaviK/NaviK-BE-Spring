package navik.domain.growthLog.service.command;

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

import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@ExtendWith(MockitoExtension.class)
class GrowthLogEvaluationApplyServiceTest {

	@Mock
	GrowthLogRepository growthLogRepository;

	@Mock
	GrowthLogPersistenceService persistence;

	@InjectMocks
	GrowthLogEvaluationApplyService service;

	@Nested
	@DisplayName("startProcessing()")
	class StartProcessing {

		@Test
		@DisplayName("PENDING 상태에서 PROCESSING으로 전환 성공")
		void success() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PENDING, null);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.updateStatusIfMatchAndToken(
				userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
			)).willReturn(1);

			// when
			service.startProcessing(growthLogId,
				new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token));

			// then
			verify(growthLogRepository).updateStatusIfMatchAndToken(
				userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
			);
		}

		@Test
		@DisplayName("이미 COMPLETED 상태면 즉시 return (멱등)")
		void alreadyCompleted_returnImmediately() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.COMPLETED, null);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			service.startProcessing(growthLogId,
				new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token));

			// then
			verify(growthLogRepository, never()).updateStatusIfMatchAndToken(
				anyLong(), anyLong(), any(), any(), anyString()
			);
		}

		@Test
		@DisplayName("이미 같은 토큰으로 PROCESSING 상태면 즉시 return (멱등)")
		void alreadyProcessingWithSameToken_returnImmediately() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when
			service.startProcessing(growthLogId,
				new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token));

			// then
			verify(growthLogRepository, never()).updateStatusIfMatchAndToken(
				anyLong(), anyLong(), any(), any(), anyString()
			);
		}

		@Test
		@DisplayName("다른 토큰으로 PROCESSING 상태면 상태 전환 시도 후 실패")
		void processingWithDifferentToken_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "new-token";
			String existingToken = "existing-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, existingToken);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.updateStatusIfMatchAndToken(
				userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
			)).willReturn(0);

			// when & then
			assertThatThrownBy(() ->
				service.startProcessing(growthLogId,
					new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token))
			).isInstanceOf(GeneralExceptionHandler.class);
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 예외를 던진다")
		void notFound_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 999L;
			String token = "test-token";

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() ->
				service.startProcessing(growthLogId,
					new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token))
			).isInstanceOf(GeneralExceptionHandler.class);
		}

		@Test
		@DisplayName("상태 전환 실패 시 예외를 던진다")
		void statusTransitionFailed_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.FAILED, null);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.updateStatusIfMatchAndToken(
				userId, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token
			)).willReturn(0);

			// when & then
			assertThatThrownBy(() ->
				service.startProcessing(growthLogId,
					new GrowthLogInternalProcessingStartRequest(userId, "trace-id", token))
			).isInstanceOf(GeneralExceptionHandler.class);
		}
	}

	@Nested
	@DisplayName("applyResult()")
	class ApplyResult {

		@Test
		@DisplayName("성공 시 persistence 호출 후 토큰 정리")
		void success() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId,
				"trace-id",
				token,
				"제목",
				"내용",
				List.of(new GrowthLogInternalApplyEvaluationRequest.KpiDelta(100L, 3)),
				List.of()
			);

			// when
			service.applyResult(growthLogId, req);

			// then
			verify(persistence).completeGrowthLogAfterProcessing(
				eq(userId), eq(growthLogId), any(), eq(3)
			);
			verify(growthLogRepository).clearProcessingTokenIfMatch(
				userId, growthLogId, token, GrowthLogStatus.COMPLETED
			);
		}

		@Test
		@DisplayName("이미 COMPLETED 상태면 즉시 return (멱등)")
		void alreadyCompleted_returnImmediately() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.COMPLETED, null);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId, "trace-id", token, "제목", "내용", List.of(), List.of()
			);

			// when
			service.applyResult(growthLogId, req);

			// then
			verify(growthLogRepository, never()).acquireApplyLock(anyLong(), anyLong(), anyString());
			verify(persistence, never()).completeGrowthLogAfterProcessing(
				anyLong(), anyLong(), any(), anyInt()
			);
		}

		@Test
		@DisplayName("lock 획득 실패 시 즉시 return")
		void lockAcquisitionFailed_returnImmediately() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(0);

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId, "trace-id", token, "제목", "내용", List.of(), List.of()
			);

			// when
			service.applyResult(growthLogId, req);

			// then
			verify(persistence, never()).completeGrowthLogAfterProcessing(
				anyLong(), anyLong(), any(), anyInt()
			);
		}

		@Test
		@DisplayName("토큰 불일치 시 예외를 던진다")
		void tokenMismatch_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "new-token";
			String existingToken = "existing-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, existingToken);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId, "trace-id", token, "제목", "내용", List.of(), List.of()  // abilities 추가
			);

			// when & then
			assertThatThrownBy(() -> service.applyResult(growthLogId, req))
				.isInstanceOf(GeneralExceptionHandler.class);
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 예외를 던진다")
		void notFound_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 999L;
			String token = "test-token";

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.empty());

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId, "trace-id", token, "제목", "내용", List.of(), List.of()  // abilities 추가
			);

			// when & then
			assertThatThrownBy(() -> service.applyResult(growthLogId, req))
				.isInstanceOf(GeneralExceptionHandler.class);
		}

		@Test
		@DisplayName("여러 KPI delta 합산이 정확하다")
		void multipleKpiDeltas_sumCorrectly() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;
			String token = "test-token";

			GrowthLog growthLog = mockGrowthLog(GrowthLogStatus.PROCESSING, token);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(growthLogRepository.acquireApplyLock(userId, growthLogId, token))
				.willReturn(1);

			var req = new GrowthLogInternalApplyEvaluationRequest(
				userId,
				"trace-id",
				token,
				"제목",
				"내용",
				List.of(
					new GrowthLogInternalApplyEvaluationRequest.KpiDelta(100L, 3),
					new GrowthLogInternalApplyEvaluationRequest.KpiDelta(101L, 5),
					new GrowthLogInternalApplyEvaluationRequest.KpiDelta(102L, -2)
				),
				List.of()
			);

			// when
			service.applyResult(growthLogId, req);

			// then
			verify(persistence).completeGrowthLogAfterProcessing(
				eq(userId), eq(growthLogId), any(), eq(6)
			);
		}
	}

	// -------------------------
	// Helpers
	// -------------------------

	private GrowthLog mockGrowthLog(GrowthLogStatus status, String processingToken) {
		GrowthLog growthLog = mock(GrowthLog.class);
		lenient().when(growthLog.getStatus()).thenReturn(status);
		lenient().when(growthLog.getProcessingToken()).thenReturn(processingToken);
		return growthLog;
	}
}