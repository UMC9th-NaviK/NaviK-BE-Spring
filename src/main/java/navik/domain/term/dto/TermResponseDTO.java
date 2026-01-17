package navik.domain.term.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

public class TermResponseDTO {

	public record TermInfo(
		Long id,
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
