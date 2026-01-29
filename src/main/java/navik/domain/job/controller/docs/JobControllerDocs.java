package navik.domain.job.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import navik.domain.job.dto.JobResponseDTO;
import navik.domain.users.exception.code.JobErrorCode;
import navik.global.swagger.ApiErrorCodes;

public interface JobControllerDocs {

	@Operation(
		summary = "직무 목록 전체 조회",
		description = "등록된 모든 직무 목록을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				array = @ArraySchema(
					schema = @Schema(implementation = JobResponseDTO.JobItem.class)
				)
			)
		)
	})
	navik.global.apiPayload.ApiResponse<List<JobResponseDTO.JobItem>> getJobs();

	@ApiErrorCodes(
		enumClass = JobErrorCode.class,
		includes = {"JOB_NOT_FOUND"}
	)
	@Operation(
		summary = "직무 단건 조회",
		description = "jobId에 해당하는 직무 정보를 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = JobResponseDTO.JobItem.class)
			)
		)
	})
	navik.global.apiPayload.ApiResponse<JobResponseDTO.JobItem> getJob(
		@Parameter(description = "직무 ID", example = "1", required = true)
		@PathVariable Long jobId
	);
}
