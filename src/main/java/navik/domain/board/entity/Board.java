package navik.domain.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "boards")
public class Board extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "article_title", nullable = false)
	private String articleTitle;

	@Column(name = "article_content", nullable = false)
	private String articleContent;

	@Column(name = "article_views", nullable = false)
	@Builder.Default
	private Integer articleViews = 0;

	@Column(name = "article_likes", nullable = false)
	@Builder.Default
	private Integer articleLikes = 0;

	public void incrementArticleViews() {
		this.articleViews++;
	}

	public void incrementArticleLikes() {
		this.articleLikes++;
	}

	public void decrementArticleLikes() {
		if (this.articleLikes > 0) {
			this.articleLikes--;
		}
	}

	public void updateBoard(String articleTitle, String articleContent) {
		this.articleTitle = articleTitle;
		this.articleContent = articleContent;
	}
}
