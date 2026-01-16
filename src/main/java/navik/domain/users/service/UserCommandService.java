package navik.domain.users.service;

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

	@Transactional
	public UserResponseDTO.BasicInfoDto updateBasicInfo(Long userId, UserRequestDTO.BasicInfoDto req) {
		User user = userQueryService.getUser(userId);
		Job job = jobRepository.getReferenceById(req.jobId());
		user.updateBasicInfo(req.name(), req.nickname(), req.isEntryLevel(), job);

		return new UserResponseDTO.BasicInfoDto(
			user.getId(),
			user.getName(),
			user.getNickname(),
			job.getId(),
			user.getIsEntryLevel()
		);
	}
}
