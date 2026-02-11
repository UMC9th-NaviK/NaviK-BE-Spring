package navik.domain.level.dto;

public class LevelResponseDTO {

	public record LevelResult(
		int levelValue,
		String description,
		int percentage
	) {
	}

}
