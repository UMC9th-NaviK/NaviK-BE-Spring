package navik.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.users.converter.UserConverter;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserCommandService {
	private final JobRepository jobRepository;
	private final UserQueryService userQueryService;

	@Transactional
	public UserResponseDTO.BasicInfoDto updateBasicInfo(Long userId, UserRequestDTO.BasicInfoDto req) {
		User user = userQueryService.getUser(userId);
		Job job = jobRepository.getReferenceById(req.getJobId());
		user.updateBasicInfo(req.getName(), req.getNickname(), req.isEntryLevel(), job);

		return UserConverter.toBasicInfoDto(user);
	}
}
