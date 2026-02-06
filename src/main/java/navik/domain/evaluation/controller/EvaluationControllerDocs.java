package navik.domain.evaluation.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.evaluation.dto.EvaluationMyDTO;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.exception.code.EvaluationErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDto;
import navik.global.swagger.ApiErrorCodes;

@Tag(name = "Evaluation", description = "스터디 평가 관련 API")
public interface EvaluationControllerDocs {

	@Operation(summary = "스터디 평가 팀원 조회 API", description = "특정 스터디에서 본인을 제외한 평가 대상 팀원 목록을 조회합니다.")
	@Parameters({
		@Parameter(name = "studyId", description = "스터디의 ID (Path Variable)", example = "1"),
		@Parameter(name = "userId", hidden = true)
	})
	@ApiErrorCodes(
		enumClass = EvaluationErrorCode.class,
		includes = {"STUDY_NOT_FOUND"}
	)
	ApiResponse<EvaluationStudyUserDTO.EvaluationPage> getTargets(
		@PathVariable Long studyId,
		@AuthUser Long userId
	);

	@Operation(summary = "스터디 평가 제출 API", description = "특정 스터디의 팀원들에 대한 평가 데이터를 제출합니다.")
	@Parameters({
		@Parameter(name = "studyId", description = "스터디의 ID (Path Variable)", example = "1"),
		@Parameter(name = "userId", hidden = true)
	})
	@ApiErrorCodes(
		enumClass = EvaluationErrorCode.class,
		includes = {
			"STUDY_NOT_FOUND",
			"USER_NOT_FOUND",
			"EVALUATION_ALREADY_EXISTS",
			"TAG_NOT_FOUND",
			"INVALID_TAG_TYPE"
		}
	)
	ApiResponse<String> submit(
		@PathVariable Long studyId,
		@AuthUser Long userId,
		@RequestBody @Valid EvaluationSubmitDTO request
	);

	@Operation(summary = "나의 누적 평가 요약 조회 API", description = "전체 스터디에서 받은 누적 평균 평점과 강점/보완점 TOP 3를 조회합니다.")
	@Parameters({
		@Parameter(name = "userId", hidden = true)
	})
	ApiResponse<EvaluationMyDTO> getMyEvaluation(Long userId);

	@Operation(summary = "스터디 평가 목록 조회 API", description = "참여한 스터디 리스트를 커서 기반 페이징으로 조회합니다.")
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회된 StudyUser의 ID", example = "10"),
		@Parameter(name = "size", description = "한 번에 가져올 데이터 개수", example = "10"),
		@Parameter(name = "userId", hidden = true)
	})
	@ApiErrorCodes(
		enumClass = EvaluationErrorCode.class,
		includes = {"USER_NOT_FOUND"}
	)
	ApiResponse<CursorResponseDto<EvaluationMyDTO.MyStudyEvaluationPreviewDTO>> getMyStudyEvaluation(
		@AuthUser Long userId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int size
	);

	@Operation(summary = "스터디 상세 평가 조회 API", description = "특정 스터디의 진행 기간, 인원, 태그 및 조언 상세 내용을 조회합니다.")
	@Parameters({
		@Parameter(name = "studyId", description = "조회할 스터디의 ID (Path Variable)", example = "1"),
		@Parameter(name = "userId", hidden = true)
	})
	@ApiErrorCodes(
		enumClass = EvaluationErrorCode.class,
		includes = {"STUDY_NOT_FOUND", "USER_NOT_FOUND"}
	)
	ApiResponse<EvaluationMyDTO.MyStudyEvaluationDetailDTO> getMyStudyDetail(
		@AuthUser Long userId,
		@PathVariable Long studyId
	);
}
