package navik.domain.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import navik.domain.notification.entity.RecommendedRecruitment;
import navik.domain.notification.exception.code.RecommendedRecruitmentErrorCode;
import navik.domain.notification.repository.RecommendedRecruitmentRepository;
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
@Transactional
public class NotificationFacadeService {

	private final UserRepository userRepository;
	private final RecruitmentRepository recruitmentRepository;
	private final NotificationConfig notificationConfig;
	private final NotificationCommandService notificationCommandService;
	private final RecommendedRecruitmentRepository recommendedRecruitmentRepository;

	public void createRecommendedRecruitmentNotification(Long userId) {

		// 1. 유저 정보 획득
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

		// 2. 조회
		List<RecommendedRecruitmentProjection> recommendedPost = recruitmentRepository.findRecommendedPosts(
			user,
			user.getJob(),
			user.getEducationLevel(),
			user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED,
			majorTypes,
			PageRequest.of(0, 1)
		);

		// 3. fit한 추천 공고가 없는 경우 처리
		if (recommendedPost.isEmpty())
			return;

		// 4. 추천 공고 저장
		Recruitment recruitment = recommendedPost.getFirst().getRecruitment();
		RecommendedRecruitment recommendedRecruitment = RecommendedRecruitment.builder()
			.recruitment(recruitment)
			.user(user)
			.build();
		recommendedRecruitmentRepository.save(recommendedRecruitment);
	}

	public void sendRecommendedRecruitmentNotification(Long recommendedRecruitmentNotificationId) {

		// 1. 추천 공고 엔티티 검색
		RecommendedRecruitment recommendedRecruitment = recommendedRecruitmentRepository.findByIdWithUserAndRecruitment(
				recommendedRecruitmentNotificationId)
			.orElseThrow(() -> new GeneralExceptionHandler(
				RecommendedRecruitmentErrorCode.RECOMMENDED_RECRUITMENT_NOT_FOUND));

		// 2. D-DAY 날짜
		List<Integer> notificationDays = notificationConfig.getNotificationDays(NotificationType.RECRUITMENT);
		List<LocalDate> targetEndDates = notificationDays.stream()
			.map(days -> LocalDate.now().plusDays(days))
			.toList();

		// 3. 상시 모집 처리
		LocalDateTime recruitmentEndDate = recommendedRecruitment.getRecruitment().getEndDate();
		if (recruitmentEndDate == null) {
			recommendedRecruitmentRepository.deleteById(recommendedRecruitmentNotificationId);
			return;
		}

		// 4. 설정된 D-DAY에 일치해야 알림 전송
		Optional<LocalDate> localDate = targetEndDates.stream()
			.filter(endDate -> endDate.isEqual(recruitmentEndDate.toLocalDate()))
			.findFirst();
		localDate.ifPresent(date -> notificationCommandService.createDeadlineNotification(
			recommendedRecruitment.getUser().getId(),    // 트랜잭션 참여
			recommendedRecruitment.getRecruitment(),
			date)
		);

		// 5. 추천 공고 삭제
		recommendedRecruitmentRepository.deleteById(recommendedRecruitmentNotificationId);
	}
}
