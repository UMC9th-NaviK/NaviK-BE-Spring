package navik.domain.recruitment.converter;

import navik.domain.recruitment.entity.Recruitment;
import navik.global.ai.dto.LLMResponseDTO;

public class RecruitmentConverter {

	public static Recruitment toEntity(LLMResponseDTO.Recruitment dto) {
		return Recruitment.builder()
			.link(dto.getLink())
			.title(dto.getTitle())
			.postId(dto.getPostId())
			.companyName(dto.getCompanyName())
			.companyLogo(dto.getCompanyLogo())
			.companySize(dto.getCompanySize())
			.industryType(dto.getIndustryType())
			.startDate(dto.getStartDate())
			.endDate(dto.getEndDate())
			.build();
	}
}

