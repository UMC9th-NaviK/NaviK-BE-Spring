package navik.domain.growthLog.service.command.strategy;

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

import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.internal.Evaluated;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.domain.job.entity.Job;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class SyncGrowthLogEvaluationStrategyTest {

	@Mock
	GrowthLogRepository growthLogRepository;

	@Mock
	GrowthLogEvaluationCoreService core;

	@Mock
	GrowthLogPersistenceService growthLogPersistenceService;

	@Mock
	RetryRateLimiter retryRateLimiter;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	SyncGrowthLogEvaluationStrategy strategy;

	// -------------------------
	// 공통 상수
	// -------------------------
	private static final Long USER_ID = 1L;
	private static final Long JOB_ID = 50L;
	private static final Integer USER_LEVEL = 3;

	@Nested
	@DisplayName("create()")
	class Create {

		@Test
		@DisplayName("성공 시 COMPLETED 상태로 반환한다")
		void success() {
			// given
			String input = "오늘의 성장 기록";

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
				"제목",
				"내용",
				List.of(new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(100L, 3)),
				List.of()
			);
			var evaluated = new Evaluated(normalized, 3);

			given(core.buildContext(eq(USER_ID), eq(input))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);
			given(growthLogPersistenceService.saveUserInputLog(eq(USER_ID), eq(normalized), eq(3)))
				.willReturn(999L);

			// when
			var result = strategy.create(USER_ID, new GrowthLogRequestDTO.CreateUserInput(input));

			// then
			assertThat(result.id()).isEqualTo(999L);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.COMPLETED);

			verify(core).buildContext(USER_ID, input);
			verify(core).evaluate(USER_ID, JOB_ID, USER_LEVEL, context);
			verify(growthLogPersistenceService).saveUserInputLog(eq(USER_ID), eq(normalized), eq(3));
			verify(growthLogPersistenceService, never()).saveFailedUserInputLog(anyLong(), anyString());
		}

		@Test
		@DisplayName("빈 입력은 '(내용 없음)'으로 변환된다")
		void emptyInput_convertedToDefault() {
			// given
			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", List.of(), List.of());
			var evaluated = new Evaluated(normalized, 0);

			given(core.buildContext(eq(USER_ID), eq("(내용 없음)"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);
			given(growthLogPersistenceService.saveUserInputLog(eq(USER_ID), any(), anyInt()))
				.willReturn(1L);

			// when
			strategy.create(USER_ID, new GrowthLogRequestDTO.CreateUserInput("   "));

			// then
			verify(core).buildContext(USER_ID, "(내용 없음)");
		}

		@Test
		@DisplayName("buildContext 예외 발생 시 FAILED 상태로 저장한다")
		void buildContextException_resultsInFailed() {
			// given
			String input = "입력";

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			given(core.buildContext(eq(USER_ID), eq(input)))
				.willThrow(new RuntimeException("DB error"));
			given(growthLogPersistenceService.saveFailedUserInputLog(eq(USER_ID), eq(input)))
				.willReturn(123L);

			// when
			var result = strategy.create(USER_ID, new GrowthLogRequestDTO.CreateUserInput(input));

			// then
			assertThat(result.id()).isEqualTo(123L);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.FAILED);

			verify(growthLogPersistenceService).saveFailedUserInputLog(USER_ID, input);
			verify(growthLogPersistenceService, never()).saveUserInputLog(anyLong(), any(), anyInt());
		}

		@Test
		@DisplayName("evaluate 예외 발생 시 FAILED 상태로 저장한다")
		void evaluateException_resultsInFailed() {
			// given
			String input = "입력";

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);

			given(core.buildContext(eq(USER_ID), eq(input))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context)))
				.willThrow(new RuntimeException("AI down"));
			given(growthLogPersistenceService.saveFailedUserInputLog(eq(USER_ID), eq(input)))
				.willReturn(456L);

			// when
			var result = strategy.create(USER_ID, new GrowthLogRequestDTO.CreateUserInput(input));

			// then
			assertThat(result.id()).isEqualTo(456L);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.FAILED);
		}

		@Test
		@DisplayName("persistence 저장 예외 발생 시 FAILED 상태로 저장한다")
		void persistenceException_resultsInFailed() {
			// given
			String input = "입력";

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", List.of(), List.of());
			var evaluated = new Evaluated(normalized, 0);

			given(core.buildContext(eq(USER_ID), eq(input))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);
			given(growthLogPersistenceService.saveUserInputLog(eq(USER_ID), any(), anyInt()))
				.willThrow(new RuntimeException("DB write error"));
			given(growthLogPersistenceService.saveFailedUserInputLog(eq(USER_ID), eq(input)))
				.willReturn(789L);

			// when
			var result = strategy.create(USER_ID, new GrowthLogRequestDTO.CreateUserInput(input));

			// then
			assertThat(result.id()).isEqualTo(789L);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.FAILED);
		}
	}

	@Nested
	@DisplayName("retry()")
	class Retry {

		@Test
		@DisplayName("성공 시 COMPLETED 상태로 반환한다")
		void success() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT, "원본 내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				USER_ID, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", List.of(), List.of());
			var evaluated = new Evaluated(normalized, 0);

			given(core.buildContext(eq(USER_ID), eq("원본 내용"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);

			// when
			var result = strategy.retry(USER_ID, growthLogId);

			// then
			assertThat(result.growthLogId()).isEqualTo(growthLogId);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.COMPLETED);

			verify(growthLogPersistenceService).updateGrowthLogAfterRetry(
				eq(USER_ID), eq(growthLogId), eq(normalized), eq(0)
			);
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 예외를 던진다")
		void notFound_throwsException() {
			// given
			Long growthLogId = 999L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> strategy.retry(USER_ID, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("USER_INPUT 타입이 아니면 예외를 던진다")
		void invalidType_throwsException() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.PORTFOLIO, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));

			// when & then
			assertThatThrownBy(() -> strategy.retry(USER_ID, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("Rate limit 초과 시 예외를 던진다")
		void rateLimitExceeded_throwsException() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> strategy.retry(USER_ID, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("상태가 FAILED가 아니면 예외를 던진다")
		void invalidStatus_throwsException() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT, "내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				USER_ID, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(0);

			// when & then
			assertThatThrownBy(() -> strategy.retry(USER_ID, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("평가 중 예외 발생 시 FAILED로 롤백하고 FAILED 상태로 반환한다")
		void evaluateException_rollbackToFailed() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT, "원본 내용");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				USER_ID, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);

			given(core.buildContext(eq(USER_ID), eq("원본 내용"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context)))
				.willThrow(new RuntimeException("AI error"));

			// when
			var result = strategy.retry(USER_ID, growthLogId);

			// then
			assertThat(result.growthLogId()).isEqualTo(growthLogId);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.FAILED);

			verify(growthLogRepository).updateStatusIfMatch(
				USER_ID, growthLogId, GrowthLogStatus.PENDING, GrowthLogStatus.FAILED
			);
		}

		@Test
		@DisplayName("빈 content는 '(내용 없음)'으로 변환된다")
		void emptyContent_convertedToDefault() {
			// given
			Long growthLogId = 10L;

			mockUser(USER_ID, USER_LEVEL, JOB_ID);

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT, "  ");

			given(growthLogRepository.findByIdAndUserId(growthLogId, USER_ID))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				USER_ID, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(1);

			var context = mock(GrowthLogAiRequestDTO.GrowthLogEvaluationContext.class);
			var normalized = new GrowthLogAiResponseDTO.GrowthLogEvaluationResult("제목", "내용", List.of(), List.of());
			var evaluated = new Evaluated(normalized, 0);

			given(core.buildContext(eq(USER_ID), eq("(내용 없음)"))).willReturn(context);
			given(core.evaluate(eq(USER_ID), eq(JOB_ID), eq(USER_LEVEL), eq(context))).willReturn(evaluated);

			// when
			strategy.retry(USER_ID, growthLogId);

			// then
			verify(core).buildContext(USER_ID, "(내용 없음)");
		}
	}

	// -------------------------
	// Helpers
	// -------------------------

	private GrowthLog mockGrowthLog(GrowthType type, String content) {
		GrowthLog growthLog = mock(GrowthLog.class);
		lenient().when(growthLog.getType()).thenReturn(type);
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