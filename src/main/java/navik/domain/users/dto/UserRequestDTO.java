package navik.domain.users.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import navik.domain.users.entity.Department;
import navik.domain.users.enums.EducationLevel;

public class UserRequestDTO {

	public record BasicInfoDto(
		@NotBlank String name,
		@NotBlank String nickname,
		@NotNull Long jobId,
		@NotNull Boolean isEntryLevel
	) {}

	public record MyInfoDto(
		String nickname,
		Boolean isEntryLevel,
		EducationLevel educationLevel,
		List<Long> departmentIds
	) {}
}
