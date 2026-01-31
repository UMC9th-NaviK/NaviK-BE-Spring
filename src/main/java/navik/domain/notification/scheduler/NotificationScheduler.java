package navik.domain.notification.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.goal.entity.Goal;
import navik.domain.goal.repository.GoalRepository;
import navik.domain.notification.config.NotificationConfig;
import navik.domain.notification.entity.NotificationType;
import navik.domain.notification.repository.NotificationRepository;
import navik.domain.notification.repository.RecommendedRecruitmentRepository;
import navik.domain.notification.service.NotificationCommandService;
import navik.domain.notification.service.NotificationFacadeService;
import navik.domain.study.repository.StudyRepository;
import navik.domain.users.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final GoalRepository goalRepository;
	private final NotificationCommandService notificationCommandService;
	private final NotificationConfig notificationConfig;
	private final NotificationFacadeService notificationFacadeService;
	private final UserRepository userRepository;
	private final RecommendedRecruitmentRepository recommendedRecruitmentRepository;
	private final StudyRepository studyRepository;
	private final NotificationRepository notificationRepository;

	/**
	 * 매일 오전 9시에 알림 대상 목표의 마감일을 체크하여 D-day 알림 생성
	 * NotificationConfig에 설정된 날짜(예: D-1, D-3, D-7)에 해당하는 목표만 조회
	 */
	@Scheduled(cron = "0 0 9 * * *")
	public void checkGoalDeadlines() {
		log.info("[NotificationScheduler] 목표 마감일 알림 스케쥴러 실행");

		List<Integer> notificationDays = notificationConfig.getNotificationDays(NotificationType.GOAL);

		List<LocalDate> targetEndDates = notificationDays.stream()
			.map(days -> LocalDate.now().plusDays(days))
			.collect(Collectors.toList());

		List<Goal> targetGoals = goalRepository.findByEndDateIn(targetEndDates);

		for (Goal goal : targetGoals) {
			try {
				notificationCommandService.createDeadlineNotification(
					goal.getUser().getId(),
					goal,
					goal.getEndDate()
				);
			} catch (Exception e) {
				log.error("❌ 알림 생성 실패 - Goal ID: {}, User ID: {}", goal.getId(), goal.getUser().getId(), e);
			}
		}

		log.info("[NotificationScheduler] 목표 마감일 알림 스케쥴러 완료");
	}

	/**
	 * 매일 오전 8시, 전날 종료된 스터디들에 대한 알림을 생성합니다.
	 */
	@Scheduled(cron = "0 0 8 * * *")
	public void checkStudyCompleted() {
		log.info("[NotificationScheduler] 스터디 완료 알림 스케쥴러 실행");

		LocalDate yesterday = LocalDate.now().minusDays(1);
		studyRepository.findAllIdsByEndDateWithStudyUser(yesterday).forEach(
			studyId -> {
				try {
					notificationFacadeService.sendStudyCompletionNotification(studyId);
				} catch (Exception e) {
					log.error("❌ 스터디 완료 알림 생성 실패 - Study ID: {}", studyId, e);
				}
			}
		);

		log.info("[NotificationScheduler] 스터디 완료 알림 스케쥴러 완료");
	}

	/**
	 * 매일 자정, 7일 이상 지난 알림을 삭제합니다.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void deleteExpiredNotifications() {
		log.info("[NotificationScheduler] 만료 알림 삭제 스케쥴러 실행");

		notificationRepository.findAllIdsByCreatedAtBefore(LocalDate.now().minusDays(7)).forEach(
			notificationId -> {
				try {
					notificationCommandService.deleteNotification(notificationId);
				} catch (Exception e) {
					log.error("❌ 만료 알림 삭제 실패 - Notification ID: {}", notificationId, e);
				}
			}
		);

		log.info("[NotificationScheduler] 만료 알림 삭제 스케쥴러 완료");
	}

	/**
	 * 매일 새벽 4시 모든 유저에 대해 추천 공고를 한 건씩 생성합니다.
	 */
	@Scheduled(cron = "0 0 4 * * *")
	public void createRecommendedRecruitment() {
		log.info("[NotificationScheduler] 추천 공고 생성 스케쥴러 실행");

		// todo: 청크 단위 Batch 처리
		userRepository.findAllIds().forEach(
			userId -> {
				try {
					notificationFacadeService.createRecommendedRecruitmentNotification(userId);
				} catch (Exception e) {
					log.error("추천 공고 생성 실패 - User ID: {}", userId, e);
				}
			}
		);

		log.info("[NotificationScheduler] 추천 공고 생성 스케쥴러 완료");
	}

	/**
	 * 매일 오전 10시 모든 유저에 대해 추천 공고를 알림으로 생성합니다.
	 * D-1 or D-5 남은 경우 생성합니다.
	 */
	@Scheduled(cron = "0 0 10 * * *")
	public void sendRecommendedRecruitment() {
		log.info("[NotificationScheduler] 추천 공고 알림 스케쥴러 실행");

		// 메모리 고려 id만 가져오고, 개별 트랜잭션을 타도록 facade
		// todo: 청크 단위 Batch 처리
		recommendedRecruitmentRepository.findAllIds().forEach(
			recommendedRecruitmentId -> {
				try {
					notificationFacadeService.sendRecommendedRecruitmentNotification(recommendedRecruitmentId);
				} catch (Exception e) {
					log.error("추천 공고 알림 전송 실패 - RecommendedRecruitment ID: {}", recommendedRecruitmentId, e);
				}
			}
		);

		log.info("[NotificationScheduler] 추천 공고 알림 스케쥴러 완료");
	}
}
