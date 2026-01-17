package navik.global.apiPayload.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.BaseCode;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

/**
 * @RestControllerAdvice 어노테이션을 사용하여 모든 @RestController 에서 발생하는 예외를 전역적으로 처리하는 클래스입니다.
 */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	/**
	 * @param e       발생한 {@link ConstraintViolationException}
	 * @param request 현재 요청 객체
	 * @return 에러 응답 {@link ApiResponse}
	 * @Valid 어노테이션을 통한 검증 실패 시 발생하는 {@link ConstraintViolationException}을 처리합니다.
	 * 주로 @RequestParam, @PathVariable 에서 검증 실패 시 발생합니다.
	 */
	@ExceptionHandler
	public ApiResponse<Object> validation(ConstraintViolationException e, WebRequest request) {
		String errorMessage = e.getConstraintViolations().stream()
			.map(ConstraintViolation::getMessage)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

		GeneralErrorCode errorCode = GeneralErrorCode.valueOf(errorMessage);
		log.warn("ConstraintViolationException: {}", errorCode.getMessage());
		return ApiResponse.onFailure(errorCode, null);
	}

	/**
	 * @param e       발생한 {@link MethodArgumentNotValidException}
	 * @param headers 응답에 포함될 HTTP 헤더
	 * @param status  응답의 HTTP 상태 코드
	 * @param request 현재 요청 객체
	 * @return 에러 응답 {@link ResponseEntity}
	 * @Valid 어노테이션을 통한 검증 실패 시 발생하는 {@link MethodArgumentNotValidException}을 처리합니다.
	 * 주로 @RequestBody 에서 검증 실패 시 발생합니다.
	 */
	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
		HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();

		e.getBindingResult().getFieldErrors()
			.forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
				errors.merge(fieldName, errorMessage,
					(existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
			});

		log.warn("MethodArgumentNotValidException: {}", errors);
		return (ResponseEntity<Object>)(ResponseEntity<?>)ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT_VALUE,
			errors);
	}

	/**
	 * JSON Request Body 파싱 실패 시 발생하는 {@link HttpMessageNotReadableException}을 처리합니다.
	 * 주로 잘못된 형식의 JSON이나 Enum 타입 불일치 시 발생합니다.
	 *
	 * @param e       발생한 {@link HttpMessageNotReadableException}
	 * @param headers 응답에 포함될 HTTP 헤더
	 * @param status  응답의 HTTP 상태 코드
	 * @param request 현재 요청 객체
	 * @return 에러 응답 {@link ResponseEntity}
	 */
	@Override
	public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
		HttpStatusCode status, WebRequest request) {
		log.warn("HttpMessageNotReadableException: {}", e.getMessage());
		return (ResponseEntity<Object>)(ResponseEntity<?>)ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT_VALUE,
			null);
	}

	/**
	 * 처리되지 않은 모든 예외를 처리합니다.
	 * 500 Internal Server Error를 반환합니다.
	 *
	 * @param e       발생한 {@link Exception}
	 * @param request 현재 요청 객체
	 * @return 에러 응답 {@link ApiResponse}
	 */
	@ExceptionHandler
	public ApiResponse<String> exception(Exception e, WebRequest request) {
		log.error("500 Error", e);
		return ApiResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	/**
	 * 비즈니스 로직 실행 중 발생하는 커스텀 예외 {@link GeneralExceptionHandler}을 처리합니다.
	 *
	 * @param generalException 발생한 {@link GeneralExceptionHandler}
	 * @param request          현재 요청 객체
	 * @return 에러 응답 {@link ApiResponse}
	 */
	@ExceptionHandler(value = GeneralExceptionHandler.class)
	public ApiResponse<Object> onThrowException(GeneralExceptionHandler generalException, HttpServletRequest request) {
		BaseCode code = generalException.getCode();
		log.warn("GeneralException: {} - {}", code.getCode(), code.getMessage());
		return ApiResponse.onFailure(code, null);
	}

}
