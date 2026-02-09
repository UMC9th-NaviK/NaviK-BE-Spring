package navik.domain.ability.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

public class AbilityRequestDTO {

	@Getter
	@Builder
	public static class CursorRequest {
		private LocalDateTime lastCreatedAt;
		private Long lastId;
	}
}
