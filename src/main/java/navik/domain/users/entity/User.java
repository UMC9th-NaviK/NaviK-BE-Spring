package navik.domain.users.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.job.entity.Job;
import navik.domain.users.enums.EducationLevel;
import navik.domain.users.enums.Role;
import navik.domain.users.enums.UserStatus;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "users")
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(nullable = false, unique = true)
	@Builder.Default
	private String nickname = "사용자" + UUID.randomUUID().toString().substring(0, 5);

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "level_value", nullable = false)
	@Builder.Default
	private Integer level = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@JoinColumn(name = "job_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Job job;

	@Column(nullable = false)
	private String socialId; // 소셜 로그인 제공자에서 주는 ID

	@Column(nullable = false)
	private String socialType; // google, kakao, naver

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private UserStatus userStatus = UserStatus.PENDING;

	@Column(name = "is_entry_level") // true 신입, false 경력
	@Builder.Default
	private Boolean isEntryLevel = true;

	@Column(name = "education_level")
	@Enumerated(EnumType.STRING)
	private EducationLevel educationLevel;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<UserDepartment> userDepartments;

	public String getRoleKey() {
		return this.role.getKey();
	}

	public void updateBasicInfo(String name, String nickname, boolean isEntryLevel, Job job) {
		this.name = name;
		this.nickname = nickname;
		this.job = job;
		this.isEntryLevel = isEntryLevel;
		this.userStatus = UserStatus.ACTIVE;
	}
}
