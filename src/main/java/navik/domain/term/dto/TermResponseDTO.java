package navik.domain.term.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TermResponseDTO {

	public record TermInfo(
		Long id,
		String title,
		String content,
		LocalDateTime updatedAt
	) {
	}

	public record AgreementResultDTO(
		Long userId,
		List<Long> agreedTermIds
	) {
	}
}
