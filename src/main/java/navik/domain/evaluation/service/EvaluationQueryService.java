package navik.domain.evaluation.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.converter.EvaluationConverter;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.entity.Evaluation;
import navik.domain.evaluation.entity.EvaluationTag;
import navik.domain.evaluation.entity.EvaluationTagSelection;
import navik.domain.evaluation.repository.EvaluationRepository;
import navik.domain.evaluation.repository.EvaluationTagRepository;
import navik.domain.evaluation.repository.EvaluationTagSelectionRepository;
import navik.domain.study.entity.Study;
import navik.domain.study.repository.StudyRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class EvaluationQueryService {

	private final StudyRepository studyRepository;
	private final StudyUserRepository studyUserRepository;
	private final UserRepository userRepository;
	private final EvaluationRepository evaluationRepository;
	private final EvaluationTagRepository evaluationTagRepository;
	private final EvaluationTagSelectionRepository selectionTagRepository;

	/**
	 * 스터디 평가 팀원 조회(본인 제외)
	 * @param studyId
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = true)
	public EvaluationStudyUserDTO.EvaluationPage getTargetMembers(Long studyId, Long userId) {
		Study study = studyRepository.getReferenceById(studyId);

		List<User> members = studyUserRepository.findUserByStudyId(studyId).stream()
			.filter(user -> !user.getId().equals(userId))
			.toList();

		return EvaluationConverter.toPage(study, members);
	}

	@Transactional
	public void submitEvaluation(Long evaluatorId, Long studyId, EvaluationSubmitDTO.EvaluationSubmit req) {
		// 자기 자신을 평가하려는 경우, 존재하지 않는다고 예외처리
		if (evaluatorId.equals(req.getTargetUserId())) {
			throw new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND);
		}

		Study study = studyRepository.getReferenceById(studyId);
		User evaluator = userRepository.getReferenceById(evaluatorId);
		User evaluatee = userRepository.findById(req.getTargetUserId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 1. 메인 평가 내용 저장
		Evaluation evaluation = evaluationRepository.save(
			EvaluationConverter.toEvaluation(req, study, evaluator, evaluatee)
		);

		// 2. 강점, 약점 태그 ID 통합
		List<Long> allTagIds = Stream.concat(req.getStrengthTagIds().stream(), req.getWeaknessTagIds().stream())
			.toList();

		// 3. EvaluationTagSelection 매핑 데이터 저장
		List<EvaluationTagSelection> selections = allTagIds.stream()
			.map(tagId -> {
				EvaluationTag tag = evaluationTagRepository.findById(tagId)
					.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.TAG_NOT_FOUND));

				return EvaluationTagSelection.builder()
					.evaluation(evaluation)
					.tag(tag)
					.build();
			})
			.toList();

		selectionTagRepository.saveAll(selections);
	}
}
