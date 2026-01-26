package navik.domain.notification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.notification.config.NotificationConfig;
import navik.domain.notification.entity.NotificationType;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.recruitment.RecruitmentRepository;
import navik.domain.recruitment.repository.recruitment.projection.RecommendedRecruitmentProjection;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

	private final UserRepository userRepository;
	private final RecruitmentRepository recruitmentRepository;
	private final NotificationConfig notificationConfig;
	private final NotificationCommandService notificationCommandService;

	@Transactional
	public void checkRecommendedRecruitment(Long userId) {

		// 1. 날짜
		List<Integer> notificationDays = notificationConfig.getNotificationDays(NotificationType.GOAL);

		List<LocalDate> targetEndDates = notificationDays.stream()
			.map(days -> LocalDate.now().plusDays(days))
			.toList();

		// 2. 유저 정보 획득
		User user = userRepository.findByIdWithUserDepartmentAndDepartment(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		List<String> departments = user.getUserDepartments().stream()
			.map(userDepartment -> userDepartment.getDepartment().getName())
			.toList();

		// 3. 조회
		Optional<RecommendedRecruitmentProjection> recommendedPost = recruitmentRepository.findRecommendedPost(
			user,
			user.getJob(),
			user.getEducationLevel(),
			user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED,
			departments.stream().map(MajorType::valueOf).toList()
		);

		// 4. 없으면 return
		if (recommendedPost.isEmpty())
			return;

		// 5. targetEndDate에 일치해야 알림 전송
		Recruitment recruitment = recommendedPost.get().getRecruitment();
		Optional<LocalDate> localDate = targetEndDates.stream()
			.filter(endDate -> endDate.isEqual(recruitment.getEndDate().toLocalDate()))
			.findFirst();
		localDate.ifPresent(date -> notificationCommandService.createDeadlineNotification(userId, recruitment, date));
	}
}
