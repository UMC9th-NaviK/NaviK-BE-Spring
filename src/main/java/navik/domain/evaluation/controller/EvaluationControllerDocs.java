package navik.domain.evaluation.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.domain.evaluation.exception.code.EvaluationErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
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
}
