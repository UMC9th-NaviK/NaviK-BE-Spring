package navik.domain.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.notification.converter.NotificationConverter;
import navik.domain.notification.dto.NotificationResponseDTO;
import navik.domain.notification.repository.NotificationRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;

	/**
	 * 7일 전까지의 알림을 모두 조회합니다.
	 */
	public List<NotificationResponseDTO.Notification> getRecentNotifications(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

		return notificationRepository.findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
				user.getId(),
				sevenDaysAgo
			)
			.stream()
			.map(NotificationConverter::toNotification)
			.toList();
	}
}
