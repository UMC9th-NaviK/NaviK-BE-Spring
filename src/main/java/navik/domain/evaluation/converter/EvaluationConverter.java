package navik.domain.evaluation.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import navik.domain.evaluation.dto.EvaluationMyDTO;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.entity.Evaluation;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyUser;
import navik.domain.users.entity.User;
import navik.global.dto.CursorResponseDto;

@Component
public class EvaluationConverter {

	/**
	 * 스터디 평가 페이지
	 * @param study
	 * @param members
	 * @return
	 */
	public static EvaluationStudyUserDTO.EvaluationPage toPage(Study study, List<User> members) {
		List<EvaluationStudyUserDTO.TargetMember> memberDto = members.stream()
			.map(user -> EvaluationStudyUserDTO.TargetMember.builder()
				.userId(user.getId())
				.nickname(user.getNickname())
				.profileImageUrl(user.getProfileImageUrl())
				.build()
			).toList();

		return EvaluationStudyUserDTO.EvaluationPage.builder()
			.studyName(study.getTitle())
			.recruitmentStatus("종료")
			.members(memberDto)
			.build();
	}

	/**
	 * 스터디 평가
	 * @param req
	 * @param study
	 * @param evaluator
	 * @param evaluatee
	 * @return
	 */
	public static Evaluation toEvaluation(EvaluationSubmitDTO req, Study study, User evaluator,
		User evaluatee) {
		return Evaluation.builder()
			.study(study)
			.evaluator(evaluator)
			.evaluatee(evaluatee)
			.score(req.score())
			.content(req.advice())
			.build();
	}

	// 평가된 스터디 목록 조회
	public static CursorResponseDto<EvaluationMyDTO.MyStudyEvaluationPreviewDTO> toEvaluationStudyList(
		List<StudyUser> studyUsers, boolean hasNext, Long cursor
	) {
		List<EvaluationMyDTO.MyStudyEvaluationPreviewDTO> content = studyUsers.stream()
			.map(su -> EvaluationMyDTO.MyStudyEvaluationPreviewDTO.builder()
				.studyId(su.getStudy().getId())
				.studyName(su.getStudy().getTitle())
				.build())
			.toList();

		String nextCursor = (cursor != null) ? String.valueOf(cursor) : null;

		return CursorResponseDto.of(content, hasNext, nextCursor);
	}

	// 평가 상세 조회
	public static EvaluationMyDTO.MyStudyEvaluationDetailDTO toEvaluationDetail(
		StudyUser su, Study study, Double avg, List<String> strengths, List<String> improvements,
		List<String> advices, int countMember
	) {
		return EvaluationMyDTO.MyStudyEvaluationDetailDTO.builder()
			.studyName(study.getTitle())
			.startDate(su.getMemberStartDate())
			.endDate(study.getEndDate())
			.memberCount(countMember)
			.participationMethod(study.getParticipationMethod())
			.weekTime(study.getWeekTime())
			.status("종료")
			.averageScore(avg)
			.strengths(strengths)
			.improvements(improvements)
			.adviceList(advices)
			.build();
	}
}
