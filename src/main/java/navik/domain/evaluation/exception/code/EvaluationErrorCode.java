package navik.domain.evaluation.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum EvaluationErrorCode implements BaseCode {

	EVALUATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "EVALUATION_400_1", "이미 해당 팀원에 대한 평가를 완료했습니다."),
	INVALID_TAG_TYPE(HttpStatus.BAD_REQUEST, "EVALUATION_400_2", "유효하지 않은 태그 타입입니다."),

	STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_404_1", "존재하지 않는 스터디입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_404_2", "평가 대상자를 찾을 수 없습니다."),
	TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_404_3", "해당 태그를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
