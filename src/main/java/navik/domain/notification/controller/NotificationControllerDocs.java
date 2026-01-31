package navik.domain.notification.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.notification.dto.NotificationResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationControllerDocs {

	@Operation(
		summary = "최근 7일 간 알림을 모두 조회합니다.",
		description = "최근 7일 간 알림을 모두 조회합니다."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "COMMON_200 - 성공"
		)
	})
	ApiResponse<List<NotificationResponseDTO.Notification>> getRecentNotifications(@AuthUser Long userId);
}
