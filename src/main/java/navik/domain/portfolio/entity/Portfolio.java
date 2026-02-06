package navik.domain.portfolio.entity;

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
import navik.domain.users.entity.User;
import navik.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "portfolios")
public class Portfolio extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "file_url", length = 2048)
	private String fileUrl;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "input_type", nullable = false)
	private InputType inputType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@Builder.Default
	private PortfolioStatus status = PortfolioStatus.PENDING;

	@Column(name = "q_b1")
	private Integer qB1;

	@Column(name = "q_b2")
	private Integer qB2;

	@Column(name = "q_b3")
	private Integer qB3;

	@Column(name = "q_b4")
	private Integer qB4;

	@Column(name = "q_b5")
	private Integer qB5;

	public void updateStatus(PortfolioStatus status) {
		this.status = status;
	}

	public void updateAdditionalInfo(Integer qB1, Integer qB2, Integer qB3, Integer qB4, Integer qB5) {
		this.qB1 = qB1;
		this.qB2 = qB2;
		this.qB3 = qB3;
		this.qB4 = qB4;
		this.qB5 = qB5;
		this.status = PortfolioStatus.PENDING;
	}
}
