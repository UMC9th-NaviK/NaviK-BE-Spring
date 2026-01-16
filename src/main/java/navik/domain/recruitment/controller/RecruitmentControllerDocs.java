package navik.domain.recruitment.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.recruitment.dto.RecruitmentResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Recruitment", description = "채용 공고 관련 API")
public interface RecruitmentControllerDocs {

	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
		})
	@Operation(summary = "사용자 추천 채용 공고 조회", description = "사용자에게 적합한 채용 공고 최대 5건을 조회합니다.")
	ApiResponse<List<RecruitmentResponseDTO.RecommendPost>> getRecommendedPosts(@AuthUser Long userId);
}
