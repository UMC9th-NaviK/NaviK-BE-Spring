package navik.domain.growthLog.service.command;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.service.command.strategy.GrowthLogEvaluationStrategy;

@ExtendWith(MockitoExtension.class)
class GrowthLogEvaluationServiceTest {

	@Mock
	GrowthLogEvaluationStrategy strategy;

	@InjectMocks
	GrowthLogEvaluationService service;

	@Test
	@DisplayName("create 호출은 GrowthLogEvaluationStrategy로 위임된다")
	void create_delegatesToStrategy() {
		Long userId = 1L;
		var req = new GrowthLogRequestDTO.CreateUserInput("hi");

		var expected = new GrowthLogResponseDTO.CreateResult(10L, GrowthLogStatus.PENDING);
		given(strategy.create(eq(userId), any())).willReturn(expected);

		var result = service.create(userId, req);

		assertThat(result).isSameAs(expected);
		then(strategy).should().create(eq(userId), any());
	}

	@Test
	@DisplayName("retry 호출은 GrowthLogEvaluationStrategy로 위임된다")
	void retry_delegates_to_strategy() {
		Long userId = 1L;
		Long growthLogId = 10L;

		var expected = new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.PENDING);
		given(strategy.retry(userId, growthLogId)).willReturn(expected);

		var result = service.retry(userId, growthLogId);

		assertThat(result).isSameAs(expected);
		then(strategy).should().retry(userId, growthLogId);
	}
}