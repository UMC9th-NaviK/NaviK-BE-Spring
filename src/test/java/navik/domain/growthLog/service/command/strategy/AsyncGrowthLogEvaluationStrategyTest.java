package navik.domain.growthLog.service.command.strategy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.message.GrowthLogEvaluationMessage;
import navik.domain.growthLog.message.GrowthLogEvaluationPublisher;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.global.apiPayload.exception.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class AsyncGrowthLogEvaluationStrategyTest {

	@Mock
	GrowthLogRepository growthLogRepository;

	@Mock
	GrowthLogPersistenceService growthLogPersistenceService;

	@Mock
	GrowthLogEvaluationPublisher publisher;

	@Mock
	RetryRateLimiter retryRateLimiter;

	@InjectMocks
	AsyncGrowthLogEvaluationStrategy strategy;

	@Nested
	@DisplayName("create()")
	class Create {

		@Test
		@DisplayName("성공 시 PENDING 상태로 반환하고 메시지를 발행한다")
		void success() {
			// given
			Long userId = 1L;
			String input = "오늘의 성장 기록";

			given(growthLogPersistenceService.savePendingUserInputLog(eq(userId), eq(input)))
				.willReturn(100L);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(100L), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(1);

			// when
			var result = strategy.create(userId, new GrowthLogRequestDTO.CreateUserInput(input));

			// then
			assertThat(result.id()).isEqualTo(100L);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.PENDING);

			verify(growthLogPersistenceService).savePendingUserInputLog(userId, input);
			verify(publisher).publish(any(GrowthLogEvaluationMessage.class));
		}

		@Test
		@DisplayName("빈 입력은 '(내용 없음)'으로 변환된다")
		void emptyInput_convertedToDefault() {
			// given
			Long userId = 1L;

			given(growthLogPersistenceService.savePendingUserInputLog(eq(userId), eq("(내용 없음)")))
				.willReturn(100L);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(100L), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(1);

			// when
			strategy.create(userId, new GrowthLogRequestDTO.CreateUserInput("   "));

			// then
			verify(growthLogPersistenceService).savePendingUserInputLog(userId, "(내용 없음)");
		}

		@Test
		@DisplayName("토큰 설정 실패 시 FAILED로 롤백하고 예외를 던진다")
		void tokenSetFailed_throwsException() {
			// given
			Long userId = 1L;
			String input = "입력";

			given(growthLogPersistenceService.savePendingUserInputLog(eq(userId), eq(input)))
				.willReturn(100L);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(100L), anyString()))
				.willReturn(0);
			given(growthLogRepository.updateStatusIfMatch(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.FAILED)
			)).willReturn(1);

			// when & then
			assertThatThrownBy(() -> strategy.create(userId, new GrowthLogRequestDTO.CreateUserInput(input)))
				.isInstanceOf(GeneralException.class);

			verify(publisher, never()).publish(any());
		}

		@Test
		@DisplayName("상태 전환 실패 시 FAILED로 롤백하고 예외를 던진다")
		void statusTransitionFailed_throwsException() {
			// given
			Long userId = 1L;
			String input = "입력";

			given(growthLogPersistenceService.savePendingUserInputLog(eq(userId), eq(input)))
				.willReturn(100L);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(100L), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(0);
			given(growthLogRepository.updateStatusIfMatch(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.FAILED)
			)).willReturn(1);

			// when & then
			assertThatThrownBy(() -> strategy.create(userId, new GrowthLogRequestDTO.CreateUserInput(input)))
				.isInstanceOf(GeneralException.class);

			verify(publisher, never()).publish(any());
		}

		@Test
		@DisplayName("publish 실패 시 PROCESSING에서 FAILED로 롤백하고 예외를 던진다")
		void publishFailed_rollbackFromProcessingToFailed() {
			// given
			Long userId = 1L;
			String input = "입력";

			given(growthLogPersistenceService.savePendingUserInputLog(eq(userId), eq(input)))
				.willReturn(100L);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(100L), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(100L), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(1);
			willThrow(new RuntimeException("Redis error"))
				.given(publisher).publish(any(GrowthLogEvaluationMessage.class));
			given(growthLogRepository.updateStatusIfMatch(
				eq(userId), eq(100L), eq(GrowthLogStatus.PROCESSING), eq(GrowthLogStatus.FAILED)
			)).willReturn(1);

			// when & then
			assertThatThrownBy(() -> strategy.create(userId, new GrowthLogRequestDTO.CreateUserInput(input)))
				.isInstanceOf(GeneralException.class);

			// PROCESSING -> FAILED 롤백 확인
			verify(growthLogRepository).updateStatusIfMatch(
				userId, 100L, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED
			);
		}
	}

	@Nested
	@DisplayName("retry()")
	class Retry {

		@Test
		@DisplayName("성공 시 PENDING 상태로 반환하고 메시지를 발행한다")
		void success() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				userId, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(1);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(growthLogId), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(growthLogId), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(1);

			// when
			var result = strategy.retry(userId, growthLogId);

			// then
			assertThat(result.growthLogId()).isEqualTo(growthLogId);
			assertThat(result.status()).isEqualTo(GrowthLogStatus.PENDING);

			verify(publisher).publish(any(GrowthLogEvaluationMessage.class));
		}

		@Test
		@DisplayName("존재하지 않는 growthLog면 예외를 던진다")
		void notFound_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 999L;

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> strategy.retry(userId, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("USER_INPUT 타입이 아니면 예외를 던진다")
		void invalidType_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthType.PORTFOLIO);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));

			// when & then
			assertThatThrownBy(() -> strategy.retry(userId, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("Rate limit 초과 시 예외를 던진다")
		void rateLimitExceeded_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(false);

			// when & then
			assertThatThrownBy(() -> strategy.retry(userId, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("상태가 FAILED가 아니면 예외를 던진다")
		void invalidStatus_throwsException() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				userId, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(0);

			// when & then
			assertThatThrownBy(() -> strategy.retry(userId, growthLogId))
				.isInstanceOf(GeneralException.class);
		}

		@Test
		@DisplayName("publish 실패 시 PROCESSING에서 FAILED로 롤백하고 예외를 던진다")
		void publishFailed_rollbackFromProcessingToFailed() {
			// given
			Long userId = 1L;
			Long growthLogId = 10L;

			GrowthLog growthLog = mockGrowthLog(GrowthType.USER_INPUT);

			given(growthLogRepository.findByIdAndUserId(growthLogId, userId))
				.willReturn(Optional.of(growthLog));
			given(retryRateLimiter.tryAcquire(anyString(), eq(3))).willReturn(true);
			given(growthLogRepository.updateStatusIfMatch(
				userId, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING
			)).willReturn(1);
			given(growthLogRepository.overwriteProcessingToken(eq(userId), eq(growthLogId), anyString()))
				.willReturn(1);
			given(growthLogRepository.updateStatusIfMatchAndToken(
				eq(userId), eq(growthLogId), eq(GrowthLogStatus.PENDING), eq(GrowthLogStatus.PROCESSING), anyString()
			)).willReturn(1);
			willThrow(new RuntimeException("Redis error"))
				.given(publisher).publish(any(GrowthLogEvaluationMessage.class));
			given(growthLogRepository.updateStatusIfMatch(
				eq(userId), eq(growthLogId), eq(GrowthLogStatus.PROCESSING), eq(GrowthLogStatus.FAILED)
			)).willReturn(1);

			// when & then
			assertThatThrownBy(() -> strategy.retry(userId, growthLogId))
				.isInstanceOf(GeneralException.class);

			// PROCESSING -> FAILED 롤백 확인
			verify(growthLogRepository).updateStatusIfMatch(
				userId, growthLogId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED
			);
		}
	}

	// -------------------------
	// Helpers
	// -------------------------

	private GrowthLog mockGrowthLog(GrowthType type) {
		GrowthLog growthLog = mock(GrowthLog.class);
		lenient().when(growthLog.getType()).thenReturn(type);
		return growthLog;
	}
}