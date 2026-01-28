package navik.domain.study.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.NotificationType;
import navik.domain.study.enums.RecruitmentStatus;
import navik.domain.study.enums.StudySynergy;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "studies")
public class Study extends BaseEntity implements Notifiable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title; // 스터디 이름

	@Column(name = "capacity", nullable = false)
	private Integer capacity; // 모이는 사람 수

	@Column(name = "description", nullable = false)
	private String description; // 스터디 소개

	@Column(name = "gathering_period", nullable = false)
	private Integer gatheringPeriod; // 모이는 기간

	@Column(name = "participation_method", nullable = false)
	private String participationMethod; // 참여 방법

	@Column(name = "synergy_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private StudySynergy synergyType;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate; // 스터디 시작일

	@Column(name = "end_date", nullable = false)
	private LocalDateTime endDate; // 스터디 종료일

	@Column(name = "recruitment_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private RecruitmentStatus recruitmentStatus; // 모집상태

	@Column(name = "open_chat_url")
	private String openChatUrl;

	@Column(name = "week_time", nullable = false)
	private Integer weekTime; // 1주일에 몇 회

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.STUDY;
	}

	@Override
	public Long getNotifiableId() {
		return this.id;
	}

	@Override
	public boolean isCompleted() {
		return LocalDateTime.now().isAfter(this.endDate);
	}

	public RecruitmentStatus getStatus(LocalDateTime now) {
		if (now.isBefore(this.startDate)) {
			return RecruitmentStatus.RECURRING; // 모집중
		} else if (now.isAfter(this.endDate)) {
			return RecruitmentStatus.CLOSED;   // 종료
		} else {
			return RecruitmentStatus.IN_PROGRESS;    // 진행중
		}
	}

	public boolean canEvaluate(LocalDateTime now) {
		return now.isAfter(this.endDate);
	}
}
