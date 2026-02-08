package navik.domain.users.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import navik.domain.users.enums.EducationLevel;

public class UserRequestDTO {

	public record BasicInfoDTO(
		@NotBlank String name,
		String nickname,
		@NotNull Long jobId,
		@NotNull Boolean isEntryLevel
	) {
	}

	public record MyInfoDTO(
		String nickname,
		Boolean isEntryLevel,
		EducationLevel educationLevel,
		List<Long> departmentIds
	) {
	}
}
