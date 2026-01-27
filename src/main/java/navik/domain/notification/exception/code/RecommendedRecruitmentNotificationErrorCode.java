package navik.domain.notification.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;

@Getter
@AllArgsConstructor
public enum RecommendedRecruitmentNotificationErrorCode implements BaseCode {

	RECOMMENDED_RECRUITMENT_NOTIFICATION_NOT_FOUND(
		HttpStatus.BAD_REQUEST,
		"RECOMMENDED_RECRUITMENT_NOTIFICATION_404_01",
		"해당되는 추천 채용 공고가 존재하지 않습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
