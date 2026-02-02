package navik.domain.evaluation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.evaluation.dto.EvaluationMyDTO;
import navik.domain.evaluation.dto.EvaluationStudyUserDTO;
import navik.domain.evaluation.dto.EvaluationSubmitDTO;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Evaluation", description = "스터디 평가 관련 API")
public interface EvaluationControllerDocs {

	@Operation(summary = "스터디 평가 팀원 조회 API", description = "특정 스터디에서 본인을 제외한 평가 대상 팀원 목록을 조회합니다.")
	@Parameters({
		@Parameter(name = "studyId", description = "스터디의 ID (Path Variable)", example = "1"),
		@Parameter(name = "userId", hidden = true)
	})
	ApiResponse<EvaluationStudyUserDTO.EvaluationPage> getTargets(Long studyId, Long userId);

	@Operation(summary = "스터디 평가 제출 API", description = "특정 스터디의 팀원들에 대한 평가 데이터를 제출합니다.")
	@Parameters({
		@Parameter(name = "studyId", description = "스터디의 ID (Path Variable)", example = "1"),
		@Parameter(name = "userId", hidden = true)
	})
	ApiResponse<String> submit(Long studyId, Long userId, EvaluationSubmitDTO request);

	@Operation(summary = "나의 누적 평가 요약 조회 API", description = "전체 스터디에서 받은 누적 평균 평점과 강점/보완점 TOP 3를 조회합니다.")
	@Parameters({
		@Parameter(name = "userId", hidden = true)
	})
	ApiResponse<EvaluationMyDTO> getMyEvaluation(Long userId);
}
