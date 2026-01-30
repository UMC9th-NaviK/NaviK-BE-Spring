package navik.domain.recruitment.controller.recruitment;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.domain.recruitment.service.recruitment.RecruitmentQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recruitments")
public class RecruitmentController implements RecruitmentControllerDocs {

	private final RecruitmentQueryService recruitmentQueryService;

	@Override
	@GetMapping
	public ApiResponse<List<RecruitmentResponseDTO.RecommendedPost>> getRecommendedPosts(@AuthUser Long userId) {
		List<RecruitmentResponseDTO.RecommendedPost> result = recruitmentQueryService.getRecommendedPosts(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}

	@Override
	@GetMapping("/kpi-cards/{kpiCardId}")
	public ApiResponse<List<RecruitmentResponseDTO.RecommendedPost>> getRecommendedPostsByCard(
		@PathVariable Long kpiCardId) {
		List<RecruitmentResponseDTO.RecommendedPost> result = recruitmentQueryService.getRecommendedPostsByCard(
			kpiCardId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}

	// TODO: 캐싱 및 무효화
	@Override
	@GetMapping("/count")
	public ApiResponse<Long> getCountPosts() {
		Long result = recruitmentQueryService.getCountPosts();
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}
}
