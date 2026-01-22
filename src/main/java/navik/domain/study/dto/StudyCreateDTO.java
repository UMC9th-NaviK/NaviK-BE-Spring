package navik.domain.study.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.study.enums.StudySynergy;

public class StudyCreateDTO {
	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CreateDTO {
		@NotBlank(message = "스터디 제목을 입력해주세요")
		private String title;
		@NotNull
		private Integer capacity;
		@NotBlank(message = "스터디 소개를 적어주세요")
		private String description;
		@NotNull
		private Long jobId;
		@NotNull
		private Long kpiId;
		@NotNull
		private Integer gatheringPeriod;
		@NotBlank(message = "참여 방법을 선택해주세요")
		private String participationMethod;
		@NotNull
		private StudySynergy synergyType;
		@NotNull
		private LocalDateTime startDate;
		@NotNull
		private LocalDateTime endDate;
		@NotBlank(message = "오픈채팅방 링크를 입력해주세요")
		private String openChatUrl;
	}

}
