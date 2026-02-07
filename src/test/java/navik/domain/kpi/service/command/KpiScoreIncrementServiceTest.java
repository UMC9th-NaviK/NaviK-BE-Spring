package navik.domain.kpi.service.command;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import navik.domain.kpi.event.KpiScoreUpdatedEvent;
import navik.domain.kpi.repository.KpiScoreRepository;

@ExtendWith(MockitoExtension.class)
class KpiScoreIncrementServiceTest {

	@Mock
	KpiScoreRepository kpiScoreRepository;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@InjectMocks
	KpiScoreIncrementService service;

	@Test
	@DisplayName("incrementInternal 호출 시 KpiScoreUpdatedEvent가 발행된다")
	void incrementInternal_publishesEvent() {
		Long userId = 1L;
		Long kpiCardId = 10L;

		given(kpiScoreRepository.incrementScore(userId, kpiCardId, 3)).willReturn(1);

		service.incrementInternal(userId, kpiCardId, 3);

		then(eventPublisher).should().publishEvent(new KpiScoreUpdatedEvent(userId));
	}
}
