package navik.domain.goal.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.notification.entity.Notifiable;
import navik.domain.notification.entity.NotificationType;
import navik.domain.users.entity.User;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "goals")
public class Goal extends BaseEntity implements Notifiable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "status", nullable = false)
	@Builder.Default
	@Enumerated(EnumType.STRING)
	private GoalStatus status = GoalStatus.NONE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public void updateStatus(GoalStatus status) {
		this.status = status;
	}

	public void updateInfo(String title, String content, LocalDate endDate) {
		if(title != null){
			this.title = title;
		}
		if(content != null){
			this.content = content;
		}
		if(endDate != null){
			this.endDate = endDate;
		}
	}

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.GOAL;
	}

	@Override
	public Long getNotifiableId() {
		return this.id;
	}

	@Override
	public boolean isCompleted() {
		return this.status == GoalStatus.COMPLETED;
	}
}
