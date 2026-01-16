package navik.domain.term.repository.projection;

import java.time.LocalDateTime;
import java.util.List;

public interface TermInfoView {
	Long getId();

	String getContent();

	LocalDateTime getUpdatedAt();
}
