package navik.domain.study.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyErrorCode {

	JOB_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"STUDY_404_01",
		"존재하지 않는 스터디입니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
