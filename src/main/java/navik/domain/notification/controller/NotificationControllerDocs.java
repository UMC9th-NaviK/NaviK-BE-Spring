package navik.domain.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.notification.dto.NotificationResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationControllerDocs {

	@Operation(
		summary = "최근 7일 간 알림을 모두 조회합니다.",
		description = """
			최근 7일 간 알림을 모두 조회합니다.
			최신순으로 정렬되어 반환합니다.
			
			### 필드 설명
			- NotificationType
				- RECRUITMENT (채용 공고 관련 알림인 경우)
				- GOAL (목표 관련 알림인 경우)
				- STUDY (스터디 관련 알림인 경우)
			- additionalInfoPerNotificationType
				- RECRUITMENT에서는 채용 사이트 링크를 의미
				- GOAL에서는 해당 목표에 대한 식별 id를 의미
				- STUDY에서는 해당 스터디에 대한 식별 id를 의미
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "COMMON_200 - 성공"
		)
	})
	ApiResponse<List<NotificationResponseDTO.Notification>> getRecentNotifications(@AuthUser Long userId);

	@Operation(
		summary = "알림을 읽음 처리합니다.",
		description = """
			알림에 대한 고유 id (notificationId)를 입력받아, 읽음 처리합니다.
			경로 변수로 입력 받습니다.
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "COMMON_201 - 요청 성공"
		)
	})
	ApiResponse<Void> readNotification(@AuthUser Long userId, @PathVariable Long notificationId);
}
