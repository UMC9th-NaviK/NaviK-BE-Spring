package navik.domain.study.entity;

import java.time.LocalDateTime;

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
import lombok.Setter;
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.enums.StudyRole;
import navik.domain.users.entity.User;
import navik.global.entity.BaseEntity;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "study_users")
public class StudyUser extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	private Study study;

	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	private StudyRole role;

	@Column(name = "attend", nullable = false)
	@Enumerated(EnumType.STRING)
	private AttendStatus attend;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = false;

	@Column(name = "member_start_date", nullable = false)
	private LocalDateTime memberStartDate;

	@Column(name = "member_end_date")
	private LocalDateTime memberEndDate;
}
