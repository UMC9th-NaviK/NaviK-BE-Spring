package navik.domain.evaluation.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.entity.Evaluation;
import navik.domain.study.entity.Study;
import navik.domain.users.entity.User;

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
	public static Evaluation toEvaluation(EvaluationSubmitDTO.EvaluationSubmit req, Study study, User evaluator,
		User evaluatee) {
		return Evaluation.builder()
			.study(study)
			.evaluator(evaluator)
			.evaluatee(evaluatee)
			.score(req.getScore())
			.content(req.getAdvice())
			.build();
	}
}
