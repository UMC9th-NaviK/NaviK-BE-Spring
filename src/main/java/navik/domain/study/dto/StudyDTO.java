package navik.domain.study.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyDTO {
	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MyStudyDTO {
		private Long studyUserId; // 페이징용 커서 ID
		private Long studyId;
		private String title; // 스터디명
		private String description; // 스터디 소개
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private Integer capacity; // 전체 정원
		private Integer currentParticipants; // 현재 참여 인원
		private String participationMethod; // 온/오프라인
		private String openChatUrl; // 오픈채팅 링크
		private String recruitment_status; // 진행상태 (준비중/진행중/종료)
		private String role; // 내 역할 (STUDY_LEADER/STUDY_MEMBER)
		private boolean canEvaluate; // 평가하기 버튼 활성화 여부 (종료일 이후에만 true)
	}
}
