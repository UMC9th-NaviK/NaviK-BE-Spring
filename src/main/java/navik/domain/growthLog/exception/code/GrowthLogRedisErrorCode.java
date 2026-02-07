package navik.domain.growthLog.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.exception.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum GrowthLogRedisErrorCode implements BaseCode {

	STREAM_PUBLISH_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"GROWTH_LOG_REDIS_500_01",
		"성장 로그 처리 중 내부 메시지 발행에 실패했습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}