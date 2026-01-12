package navik.domain.recruitment.converter;

import navik.domain.job.entity.Job;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.Recruitment;
import navik.global.ai.dto.LLMResponseDTO;

public class PositionConverter {

	public static Position toEntity(LLMResponseDTO.Recruitment.Position dto, Recruitment recruitment, Job job) {
		return Position.builder()
			.name(dto.getName())
			.job(job)
			.employmentType(dto.getEmploymentType())
			.experienceType(dto.getExperienceType())
			.educationType(dto.getEducationType())
			.areaType(dto.getAreaType())
			.recruitment(recruitment)
			.build();
	}
}
