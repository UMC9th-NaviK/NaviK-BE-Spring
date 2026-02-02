package navik.domain.evaluation.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.converter.EvaluationConverter;
import navik.domain.evaluation.converter.EvaluationMyConverter;
import navik.domain.evaluation.dto.EvaluationMyDTO;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.entity.Evaluation;
import navik.domain.evaluation.entity.EvaluationTag;
import navik.domain.evaluation.entity.EvaluationTagSelection;
import navik.domain.evaluation.enums.TagType;
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
	public void submitEvaluation(Long evaluatorId, Long studyId, EvaluationSubmitDTO req) {
		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.STUDY_NOT_FOUND));
		User evaluator = userRepository.findById(evaluatorId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
		User evaluatee = userRepository.findById(req.targetUserId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 이미 해당 스터디원에 대한 평가를 진행했으면 오류발생
		if (evaluationRepository.existsByStudyIdAndEvaluatorIdAndEvaluateeId(studyId, evaluatorId,
			req.targetUserId())) {
			throw new GeneralExceptionHandler(GeneralErrorCode.EVALUATION_ALREADY_EXISTS);
		}

		// 1. 메인 평가 내용 저장
		Evaluation evaluation = evaluationRepository.save(
			EvaluationConverter.toEvaluation(req, study, evaluator, evaluatee)
		);

		// 2. 강점, 약점 태그 ID 통합
		List<Long> allTagIds = Stream.concat(req.strengthTagIds().stream(), req.weaknessTagIds().stream())
			.toList();
		List<EvaluationTag> tags = evaluationTagRepository.findAllById(allTagIds);

		// 3. EvaluationTagSelection 매핑 데이터 저장
		List<EvaluationTagSelection> selections = tags.stream()
			.map(tag -> EvaluationTagSelection.builder()
				.evaluation(evaluation)
				.tag(tag)
				.build())
			.toList();

		selectionTagRepository.saveAll(selections);
	}

	/**
	 * 내 평가 조회
	 * @param userId
	 * @return
	 */
	@Transactional
	public EvaluationMyDTO myEvaluation(Long userId) {
		List<Evaluation> evaluations = evaluationRepository.findAllByEvaluateeId(userId);

		// 받은 평가 없는 경우, 빈 리스트 반환
		if (evaluations.isEmpty()) {
			return EvaluationMyConverter.toEvaluationMyDTO(0.0, List.of(), List.of());
		}

		// 누적 평균 평점 계산
		Double avg = evaluations.stream()
			.mapToDouble(Evaluation::getScore)
			.average()
			.orElse(0.0);
		double averageRating = Math.round(avg * 10) / 10.0;

		// 3. 누적된 모든 태그 선택(EvaluationTagSelection) 정보 조회
		List<EvaluationTagSelection> selections = selectionTagRepository.findAllByEvaluationIn(evaluations);

		// 4. 강점 및 보완점 TOP 3 추출 로직
		List<String> topStrengths = extractTop3(selections, TagType.STRENGTH);
		List<String> topWeaknesses = extractTop3(selections, TagType.IMPROVEMENT);

		return EvaluationMyConverter.toEvaluationMyDTO(averageRating, topStrengths, topWeaknesses);
	}

	private List<String> extractTop3(List<EvaluationTagSelection> selections, TagType type) {
		return selections.stream()
			.map(EvaluationTagSelection::getTag) // 매핑 엔티티에서 실제 Tag 엔티티 추출
			.filter(tagType -> tagType.getTagType() == type)
			.collect(Collectors.groupingBy(EvaluationTag::getTagContent, Collectors.counting())) // 이름별 빈도수 계산
			.entrySet().stream()
			.sorted((a, b) -> {
				int compare = b.getValue().compareTo(a.getValue()); // 1순위: 빈도수 높은 순
				return (compare == 0) ? -1 : compare; // 2순위: 동일 빈도일 경우 최신순(입력 순서 기반)
			})
			.limit(3)
			.map(Map.Entry::getKey)
			.toList();
	}
}
