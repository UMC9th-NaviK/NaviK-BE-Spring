package navik.domain.recruitment.controller.recruitment;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.swagger.annotation.ApiErrorCodes;

@Tag(name = "Recruitment", description = "채용 공고 관련 API")
public interface RecruitmentControllerDocs {

	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")})
	@Operation(summary = "사용자 추천 채용 공고 조회", description = "사용자에게 적합한 채용 공고 최대 5건을 조회합니다.")
	ApiResponse<List<RecruitmentResponseDTO.RecommendedPost>> getRecommendedPosts(@AuthUser Long userId);

	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")})
	@ApiErrorCodes(
		enumClass = KpiCardErrorCode.class,
		includes = {
			"KPI_CARD_NOT_FOUND"
		}
	)
	@Operation(summary = "KPI 관련 채용 공고 조회", description = "KPI 카드와 관련된 채용 공고 최대 5건을 조회합니다.")
	ApiResponse<List<RecruitmentResponseDTO.RecommendedPost>> getRecommendedPostsByCard(
		@PathVariable Long kpiCardId
	);

	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")})
	@Operation(summary = "전체 채용 공고 개수 조회", description = "DB에 등록된 채용 공고의 전체 개수를 반환합니다.")
	ApiResponse<Long> getCountPosts();
}
