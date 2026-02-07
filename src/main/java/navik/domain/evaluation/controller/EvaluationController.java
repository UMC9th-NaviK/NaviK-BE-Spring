package navik.domain.evaluation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.dto.EvaluationMyDTO;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.service.EvaluationQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequestMapping("/v1/evaluations")
@RequiredArgsConstructor
public class EvaluationController implements EvaluationControllerDocs {

	private final EvaluationQueryService evaluationQueryService;

	/**
	 * 스터디 평가 팀원 조회
	 * @param studyId
	 * @param userId
	 * @return
	 */
	@GetMapping("/study/{studyId}/targets")
	public ApiResponse<EvaluationStudyUserDTO.EvaluationPage> getTargets(
		@PathVariable Long studyId,
		@AuthUser Long userId
	) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK,
			evaluationQueryService.getTargetMembers(studyId, userId));
	}

	/**
	 * 스터디 평가 제출
	 * @param studyId
	 * @param userId
	 * @param request
	 * @return
	 */
	@PostMapping("/study/{studyId}")
	public ApiResponse<String> submit(
		@PathVariable Long studyId,
		@AuthUser Long userId,
		@RequestBody @Valid EvaluationSubmitDTO request
	) {
		evaluationQueryService.submitEvaluation(userId, studyId, request);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, "성공적으로 평가가 제출되었습니다.");
	}

	/**
	 * 내 평가 조회
	 * @param userId
	 * @return
	 */
	@GetMapping("/my")
	public ApiResponse<EvaluationMyDTO> getMyEvaluation(
		@AuthUser Long userId
	) {
		EvaluationMyDTO response = evaluationQueryService.myEvaluation(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 평가된 스터디 목록 조회
	 * @param userId
	 * @param cursor
	 * @param size
	 * @return
	 */
	@GetMapping("/studies")
	public ApiResponse<CursorResponseDTO<EvaluationMyDTO.MyStudyEvaluationPreviewDTO>> getMyStudyEvaluation(
		@AuthUser Long userId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int size
	) {
		CursorResponseDTO<EvaluationMyDTO.MyStudyEvaluationPreviewDTO> response = evaluationQueryService.getMyEvaluations(
			userId, cursor, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 평가 상세 조회
	 * @param userId
	 * @param studyId
	 * @return
	 */
	@GetMapping("/studies/{studyId}")
	public ApiResponse<EvaluationMyDTO.MyStudyEvaluationDetailDTO> getMyStudyDetail(
		@AuthUser Long userId,
		@PathVariable Long studyId
	) {
		EvaluationMyDTO.MyStudyEvaluationDetailDTO response = evaluationQueryService.getMyEvaluationDetails(userId,
			studyId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}
}