package navik.domain.evaluation.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationMyDTO {
	private Double averageScore;
	private List<String> topStrengths; // 3개씩
	private List<String> topImprovement;

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MyStudyEvaluationPreviewDTO {
		private Long studyId;
		private String studyName;
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MyStudyEvaluationDetailDTO {
		private String studyName;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private Integer memberCount;
		private String participationMethod;
		private Integer weekTime;
		private String status;

		private List<String> strengths;
		private List<String> improvements;
		private List<String> adviceList;
		private Double averageScore;
	}

}
