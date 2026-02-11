package navik.domain.study.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecommendDTO {
	private Long studyId;
	private String title;
	private String description;
	private String kpiName;
	private Long kpiId;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Integer capacity; // 전체 정원
	private Integer participantCount; // 현재 참여 인원
	private String participationMethod; // 온/오프라인

}
