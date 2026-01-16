package navik.domain.users.service;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;

@Service
@RequiredArgsConstructor
public class UserCommandService {
	private final JobRepository jobRepository;
	private final UserQueryService userQueryService;
	private final ConversionService conversionService;

	@Transactional
	public UserResponseDTO.BasicInfoDto updateBasicInfo(Long userId, UserRequestDTO.BasicInfoDto req) {
		User user = userQueryService.getUser(userId);
		Job job = jobRepository.getReferenceById(req.jobId());
		user.updateBasicInfo(req.name(), req.nickname(), req.isEntryLevel(), job);

		return conversionService.convert(user, UserResponseDTO.BasicInfoDto.class);
	}
}
