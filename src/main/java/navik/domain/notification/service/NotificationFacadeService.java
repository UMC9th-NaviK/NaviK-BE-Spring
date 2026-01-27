package navik.domain.notification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFacadeService {

	private final UserRepository userRepository;
	private final RecruitmentRepository recruitmentRepository;
	private final NotificationConfig notificationConfig;
	private final NotificationCommandService notificationCommandService;

	@Transactional
	public void checkRecommendedRecruitment(Long userId) {

		// 1. 날짜
		List<Integer> notificationDays = notificationConfig.getNotificationDays(NotificationType.RECRUITMENT);

		List<LocalDate> targetEndDates = notificationDays.stream()
			.map(days -> LocalDate.now().plusDays(days))
			.toList();

		// 2. 유저 정보 획득
		User user = userRepository.findByIdWithUserDepartmentAndDepartment(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		List<String> departments = user.getUserDepartments().stream()
			.map(userDepartment -> userDepartment.getDepartment().getName())
			.toList();

		List<MajorType> majorTypes = departments.stream()
			.map(name -> {
				try {
					return MajorType.valueOf(name);
				} catch (Exception e) {
					log.error("[NotificationFacadeService] 존재하지 않는 학과 타입입니다 : {}", name, e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();

		// 3. 조회
		List<RecommendedRecruitmentProjection> recommendedPost = recruitmentRepository.findRecommendedPosts(
			user,
			user.getJob(),
			user.getEducationLevel(),
			user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED,
			majorTypes,
			PageRequest.of(0, 1)
		);

		// 4. 없으면 return
		if (recommendedPost.isEmpty())
			return;

		// 5. targetEndDate에 일치해야 알림 전송
		Recruitment recruitment = recommendedPost.getFirst().getRecruitment();
		if (recruitment.getEndDate() == null)
			return;

		Optional<LocalDate> localDate = targetEndDates.stream()
			.filter(endDate -> endDate.isEqual(recruitment.getEndDate().toLocalDate()))
			.findFirst();
		localDate.ifPresent(date -> notificationCommandService.createDeadlineNotification(userId, recruitment, date));
	}
}
