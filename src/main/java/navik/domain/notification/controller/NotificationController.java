package navik.domain.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.notification.dto.NotificationResponseDTO;
import navik.domain.notification.service.NotificationCommandService;
import navik.domain.notification.service.NotificationQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController implements NotificationControllerDocs {

	private final NotificationQueryService notificationQueryService;
	private final NotificationCommandService notificationCommandService;

	@GetMapping
	public ApiResponse<List<NotificationResponseDTO.Notification>> getRecentNotifications(@AuthUser Long userId) {
		List<NotificationResponseDTO.Notification> result = notificationQueryService.getRecentNotifications(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}

	@PatchMapping("/{notificationId}")
	public ApiResponse<Void> readNotification(@AuthUser Long userId, @PathVariable Long notificationId) {
		notificationCommandService.readNotification(userId, notificationId);
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED);
	}
}
