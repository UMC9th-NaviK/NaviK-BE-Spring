package navik.domain.study.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor

public enum StudyErrorCode implements BaseCode {

	STUDY_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "STUDY_400_1", "이미 신청한 스터디입니다"),
	STUDY_MEMBER_FULL(HttpStatus.BAD_REQUEST, "STUDY_400_2", "모집 인원이 모두 찼습니다"),
	INVALID_ATTEND_STATUS(HttpStatus.BAD_REQUEST, "STUDY_400_3", "잘못된 참여 상태 값입니다"),
	INVALID_RECRUITMENT_STATUS(HttpStatus.BAD_REQUEST, "STUDY_400_4", "잘못된 모집 상태 값입니다"),
	INVALID_STUDY_ROLE(HttpStatus.BAD_REQUEST, "STUDY_400_5", "잘못된 스터디 역할 값입니다"),
	INVALID_SYNERGY_TYPE(HttpStatus.BAD_REQUEST, "STUDY_400_6", "유효하지 않은 시너지 타입입니다."),

	NOT_STUDY_LEADER(HttpStatus.FORBIDDEN, "STUDY_403_1", "스터디장만 접근 권한이 있습니다"),

	STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404_1", "존재하지 않는 스터디입니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404_2", "존재하지 않는 사용자입니다"),
	STUDY_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404_3", "해당 스터디 신청 내역을 찾을 수 없습니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
