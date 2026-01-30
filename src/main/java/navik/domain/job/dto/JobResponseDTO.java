package navik.domain.job.dto;

public class JobResponseDTO {
	public record JobItem(
		Long id,
		String name,
		String description
	) {
	}
}
