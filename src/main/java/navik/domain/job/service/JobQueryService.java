package navik.domain.job.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.job.dto.JobResponseDTO;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.users.exception.code.JobErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobQueryService {

	private final JobRepository jobRepository;

	public List<JobResponseDTO.JobItem> getJobs() {
		return jobRepository.findAll()
			.stream()
			.map(job -> new JobResponseDTO.JobItem(
				job.getId(),
				job.getName(),
				job.getDescription()
			)).toList();
	}

	public JobResponseDTO.JobItem getJob(Long jobId) {

		Job job = jobRepository.findById(jobId)
			.orElseThrow(() -> new GeneralExceptionHandler(JobErrorCode.JOB_NOT_FOUND));

		return new JobResponseDTO.JobItem(job.getId(), job.getName(), job.getDescription());
	}
}
