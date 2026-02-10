package navik.domain.ability.dto;

import lombok.Builder;
import lombok.Getter;

public class AbilityResponseDTO {

	@Getter
	@Builder
	public static class AbilityDTO {
		private Long abilityId;
		private String content;
	}
}
