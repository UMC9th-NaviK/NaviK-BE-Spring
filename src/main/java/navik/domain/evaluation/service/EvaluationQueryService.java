package navik.domain.evaluation.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.domain.study.repository.StudyRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.dto.CursorResponseDTO;

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
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));

		List<User> members = studyUserRepository.findUserByStudyId(studyId);

		if (!members.contains(user)) {
			throw new GeneralException(StudyErrorCode.USER_NOT_FOUND);
		}

		members = members.stream()
			.filter(u -> !u.getId().equals(userId))
			.toList();

		return EvaluationConverter.toPage(study, members);
	}

	@Transactional
	public void submitEvaluation(Long evaluatorId, Long studyId, EvaluationSubmitDTO req) {
		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		User evaluator = userRepository.findById(evaluatorId)
			.orElseThrow(() -> new GeneralException(StudyErrorCode.USER_NOT_FOUND));
		User evaluatee = userRepository.findById(req.targetUserId())
			.orElseThrow(() -> new GeneralException(StudyErrorCode.USER_NOT_FOUND));

		// 이미 해당 스터디원에 대한 평가를 진행했으면 오류발생
		if (evaluationRepository.existsByStudyIdAndEvaluatorIdAndEvaluateeId(studyId, evaluatorId,
			req.targetUserId())) {
			throw new GeneralException(GeneralErrorCode.EVALUATION_ALREADY_EXISTS);
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
		List<String> topStrengths = extractTopTags(selections, TagType.STRENGTH, 3);
		List<String> topWeaknesses = extractTopTags(selections, TagType.IMPROVEMENT, 3);

		return EvaluationMyConverter.toEvaluationMyDTO(averageRating, topStrengths, topWeaknesses);
	}

	/**
	 * 평가된 스터디 목록 조회
	 * @param userId
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<EvaluationMyDTO.MyStudyEvaluationPreviewDTO> getMyEvaluations(Long userId, Long cursor,
		int pageSize) {
		Pageable pageable = PageRequest.of(0, pageSize + 1);

		List<StudyUser> myStudies = studyUserRepository.findMyStudiesByCursor(userId, cursor, pageable);

		boolean hasNext = myStudies.size() > pageSize;
		if (hasNext) {
			myStudies.remove(pageSize);
		}

		Long nextCursor = hasNext ? myStudies.get(myStudies.size() - 1).getId() : null;

		return EvaluationConverter.toEvaluationStudyList(myStudies, hasNext, nextCursor);
	}

	/**
	 * 평가 상세 조회
	 * @param userId
	 * @param studyId
	 * @return
	 */
	@Transactional(readOnly = true)
	public EvaluationMyDTO.MyStudyEvaluationDetailDTO getMyEvaluationDetails(Long userId, Long studyId) {
		// 스터디 참여했는지 확인
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudyId(userId, studyId)
			.orElseThrow(() -> new GeneralException(StudyErrorCode.USER_NOT_FOUND));

		Study study = studyUser.getStudy();

		// 스터디의 실제 인원수 계산
		int countMember = (int)studyUserRepository.countByStudyIdAndAttend(studyId, AttendStatus.ACCEPTANCE);

		// 내가 해당 스터디에서 받은 모드 평가 조회
		List<Evaluation> evaluations = evaluationRepository.findAllByEvaluateeIdAndStudyId(userId, studyId);

		if (evaluations.isEmpty()) {
			return EvaluationConverter.toEvaluationDetail(studyUser, study, 0.0, List.of(), List.of(), List.of(),
				countMember);
		}

		Double avg = evaluations.stream().mapToDouble(Evaluation::getScore).average().orElse(0.0);
		double averageRating = Math.round(avg * 10) / 10.0;

		// 모든 조언 list
		List<String> adviceList = evaluations.stream()
			.map(Evaluation::getContent)
			.toList();

		// 강점, 보완 태그 5개씩
		List<EvaluationTagSelection> selections = selectionTagRepository.findAllByEvaluationIn(evaluations);
		List<String> strengths = extractTopTags(selections, TagType.STRENGTH, 5);
		List<String> weaknesses = extractTopTags(selections, TagType.IMPROVEMENT, 5);

		return EvaluationConverter.toEvaluationDetail(studyUser, study, averageRating, strengths, weaknesses,
			adviceList, countMember);
	}

	// top3, top5
	private List<String> extractTopTags(List<EvaluationTagSelection> selections, TagType type, int limit) {
		return selections.stream()
			.map(EvaluationTagSelection::getTag)
			.filter(tag -> tag.getTagType() == type)
			.collect(Collectors.groupingBy(EvaluationTag::getTagContent, Collectors.counting()))
			.entrySet().stream()
			.sorted((a, b) -> {
				int compare = b.getValue().compareTo(a.getValue());
				return (compare == 0) ? -1 : compare;
			})
			.limit(5)
			.map(Map.Entry::getKey)
			.toList();
	}
}
