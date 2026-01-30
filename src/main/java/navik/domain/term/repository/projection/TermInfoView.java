package navik.domain.term.repository.projection;

import java.time.LocalDateTime;

public interface TermInfoView {
	Long getId();

	String getTitle();

	String getContent();

	LocalDateTime getUpdatedAt();
}
