package navik.domain.evaluation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

public class EvaluationStudyUserDTO {

	// 평가 페이지
	@Getter
	@Builder
	public static class EvaluationPage {
		private String studyName; // "스터디 이름"
		private String recruitmentStatus; // "종료"
		private List<TargetMember> members;
	}

	// 평가 멤버
	@Getter
	@Builder
	public static class TargetMember {
		private Long userId;
		private String nickname;
		private String profileImageUrl;
	}
}
