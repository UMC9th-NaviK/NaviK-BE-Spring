package navik.global.apiPayload.exception.handler;

import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

/**
 * 비즈니스 로직 실행 중 발생하는 예외를 표현하는 클래스입니다.
 * {@link BaseCode}를 포함하여, 예외에 대한 구체적인 정보를 제공합니다.
 */
@Getter
public class GeneralExceptionHandler extends RuntimeException {
	private final BaseCode code;

	public GeneralExceptionHandler(BaseCode code) {
		super(code.getMessage());
		this.code = code;
	}
}
