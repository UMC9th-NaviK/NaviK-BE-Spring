package navik.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyApplicationDTO {

	@Getter
	public static class ProcessApplicationDTO {
		private Boolean accept; // 함께하기 , 거절하기
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ApplicationPreviewDTO {
		private Long studyUserId;
		private Long userId;
		private String name;
		private String jobName;
		private Integer level;
		private float score;
		private String profileImageUrl;
	}
}
