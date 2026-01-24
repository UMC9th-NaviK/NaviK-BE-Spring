package navik.domain.recruitment.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.listener.RecruitmentConsumer;

@ExtendWith(MockitoExtension.class)
class RecruitmentPendingSchedulerTest {

	@InjectMocks
	private RecruitmentPendingScheduler scheduler;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private StreamOperations<String, Object, Object> streamOperations;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private RecruitmentConsumer recruitmentConsumer;

	private final String streamKey = "test-stream";
	private final String consumerGroupName = "test-group";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(scheduler, "streamKey", streamKey);
		ReflectionTestUtils.setField(scheduler, "consumerGroupName", consumerGroupName);
		ReflectionTestUtils.setField(scheduler, "consumerName", "test-consumer");

		given(redisTemplate.opsForStream()).willReturn(streamOperations);
	}

	@Test
	@DisplayName("Pending 메시지가 없을 경우 아무 작업도 하지 않는다")
	void retryPendingMessage_NoPendingMessages() {
		// given
		given(streamOperations.pending(streamKey, consumerGroupName)).willReturn(null);
		// when
		scheduler.retryPendingMessage();
		// then
		verify(streamOperations, never()).pending(eq(streamKey), eq(consumerGroupName), any(Range.class),
			anyLong()); // 요약본만 조회, 실제 메시지는 미호출
	}

	@Test
	@DisplayName("Pending 메시지가 있지만 처리 가능한 상태일 때 정상적으로 처리한다")
	void retryPendingMessage_Success() {
		// given
		PendingMessagesSummary summary = mock(PendingMessagesSummary.class);
		given(summary.getTotalPendingMessages()).willReturn(1L);
		given(streamOperations.pending(streamKey, consumerGroupName)).willReturn(summary);

		PendingMessage pendingMessage = mock(PendingMessage.class);
		RecordId recordId = RecordId.of("1678888888888-0");
		given(pendingMessage.getId()).willReturn(recordId);
		given(pendingMessage.getTotalDeliveryCount()).willReturn(1L);

		PendingMessages pendingMessages = new PendingMessages(consumerGroupName, List.of(pendingMessage));
		given(streamOperations.pending(eq(streamKey), eq(consumerGroupName), any(Range.class), anyLong()))
			.willReturn(pendingMessages);

		MapRecord<String, Object, Object> record = mock(MapRecord.class);
		given(record.getValue()).willReturn(Map.of("value", "{\"some\":\"json\"}"));
		given(streamOperations.claim(eq(streamKey), eq(consumerGroupName), anyString(), any(Duration.class),
			eq(recordId)))
			.willReturn(List.of(record));

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(anyString())).willReturn(null);

		RecruitmentRequestDTO.Recruitment recruitmentDTO = RecruitmentRequestDTO.Recruitment.builder().build();
		given(objectMapper.convertValue(anyString(), eq(RecruitmentRequestDTO.Recruitment.class)))
			.willReturn(recruitmentDTO);

		// when
		scheduler.retryPendingMessage();

		// then
		verify(recruitmentConsumer).saveRecruitment(recruitmentDTO); // save 호출 검증
	}

	@Test
	@DisplayName("최대 재시도 횟수를 초과한 경우 메시지를 ACK 처리하고 삭제한다")
	void retryPendingMessage_MaxRetryExceeded() {
		// given
		PendingMessagesSummary summary = mock(PendingMessagesSummary.class);
		given(summary.getTotalPendingMessages()).willReturn(1L);
		given(streamOperations.pending(streamKey, consumerGroupName)).willReturn(summary);

		PendingMessage pendingMessage = mock(PendingMessage.class);
		RecordId recordId = RecordId.of("1678888888888-0");
		given(pendingMessage.getId()).willReturn(recordId);

		PendingMessages pendingMessages = new PendingMessages(consumerGroupName, List.of(pendingMessage));
		given(streamOperations.pending(eq(streamKey), eq(consumerGroupName), any(Range.class), anyLong()))
			.willReturn(pendingMessages);

		given(streamOperations.claim(eq(streamKey), eq(consumerGroupName), anyString(), any(Duration.class),
			eq(recordId)))
			.willReturn(Collections.emptyList());

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(anyString())).willReturn(2); // MAX_RETRY_COUNT = 2

		// when
		scheduler.retryPendingMessage();

		// then
		verify(streamOperations).acknowledge(streamKey, consumerGroupName, recordId.getValue()); // ACK 호출 검증
		verify(redisTemplate).delete(anyString()); // 삭제 호출 검증
		verify(recruitmentConsumer, never()).saveRecruitment(any()); // save 미호출 검증
	}

	@Test
	@DisplayName("최대 전달 횟수를 초과한 경우 메시지를 ACK 처리하고 삭제한다")
	void retryPendingMessage_MaxDeliveryExceeded() {
		// given
		PendingMessagesSummary summary = mock(PendingMessagesSummary.class);
		given(summary.getTotalPendingMessages()).willReturn(1L);
		given(streamOperations.pending(streamKey, consumerGroupName)).willReturn(summary);

		PendingMessage pendingMessage = mock(PendingMessage.class);
		RecordId recordId = RecordId.of("1678888888888-0");
		given(pendingMessage.getId()).willReturn(recordId);
		given(pendingMessage.getTotalDeliveryCount()).willReturn(2L); // MAX_DELIVERY_COUNT = 2

		PendingMessages pendingMessages = new PendingMessages(consumerGroupName, List.of(pendingMessage));
		given(streamOperations.pending(eq(streamKey), eq(consumerGroupName), any(Range.class), anyLong()))
			.willReturn(pendingMessages);

		given(streamOperations.claim(eq(streamKey), eq(consumerGroupName), anyString(), any(Duration.class),
			eq(recordId)))
			.willReturn(Collections.emptyList());

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(anyString())).willReturn(null);

		// when
		scheduler.retryPendingMessage();

		// then
		verify(streamOperations).acknowledge(streamKey, consumerGroupName, recordId.getValue());
		verify(redisTemplate).delete(anyString());
		verify(recruitmentConsumer, never()).saveRecruitment(any());
	}

	@Test
	@DisplayName("처리 중 예외가 발생하면 에러 카운트를 증가시킨다")
	void retryPendingMessage_ExceptionDuringProcessing() {
		// given
		PendingMessagesSummary summary = mock(PendingMessagesSummary.class);
		given(summary.getTotalPendingMessages()).willReturn(1L);
		given(streamOperations.pending(streamKey, consumerGroupName)).willReturn(summary);

		PendingMessage pendingMessage = mock(PendingMessage.class);
		RecordId recordId = RecordId.of("1678888888888-0");
		given(pendingMessage.getId()).willReturn(recordId);
		given(pendingMessage.getTotalDeliveryCount()).willReturn(1L);

		PendingMessages pendingMessages = new PendingMessages(consumerGroupName, List.of(pendingMessage));
		given(streamOperations.pending(eq(streamKey), eq(consumerGroupName), any(Range.class), anyLong()))
			.willReturn(pendingMessages);

		MapRecord<String, Object, Object> record = mock(MapRecord.class);
		given(record.getValue()).willReturn(Map.of("value", "{\"some\":\"json\"}"));
		given(streamOperations.claim(eq(streamKey), eq(consumerGroupName), anyString(), any(Duration.class),
			eq(recordId)))
			.willReturn(List.of(record));

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(anyString())).willReturn(0);

		given(objectMapper.convertValue(anyString(), eq(RecruitmentRequestDTO.Recruitment.class)))
			.willThrow(new RuntimeException("Processing Error"));

		// when
		scheduler.retryPendingMessage();

		// then
		verify(valueOperations).increment(anyString());
		verify(recruitmentConsumer, never()).saveRecruitment(any());
	}
}
