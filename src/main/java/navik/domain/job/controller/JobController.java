package navik.domain.job.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.job.controller.docs.JobControllerDocs;
import navik.domain.job.dto.JobResponseDTO;
import navik.domain.job.service.JobQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/jobs")
public class JobController implements JobControllerDocs {

	private final JobQueryService jobQueryService;

	// 직무 목록 전체 조회
	@Override
	@GetMapping
	public ApiResponse<List<JobResponseDTO.JobItem>> getJobs() {
		List<JobResponseDTO.JobItem> jobs = jobQueryService.getJobs();
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, jobs);
	}

	// 직무 목록 단건 조회
	@Override
	@GetMapping("/{jobId}")
	public ApiResponse<JobResponseDTO.JobItem> getJob(@PathVariable Long jobId) {
		JobResponseDTO.JobItem job = jobQueryService.getJob(jobId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, job);
	}
}
