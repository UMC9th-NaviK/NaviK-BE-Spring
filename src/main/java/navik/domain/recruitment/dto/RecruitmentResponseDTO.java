package navik.domain.recruitment.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.IndustryType;

public class RecruitmentResponseDTO {

	@Getter
	@NoArgsConstructor
	public static class LLMResponse {
		private String link;
		private String title;
		private String postId;
		private String companyName;
		private String companyLogo;
		private CompanySize companySize;
		private IndustryType industryType;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
	}
}
