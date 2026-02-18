package navik.domain.users.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.departments.repository.DepartmentRepository;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.users.dto.UserRequestDTO;
import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;
import navik.domain.users.entity.UserDepartment;
import navik.domain.users.repository.UserDepartmentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {
	private final JobRepository jobRepository;
	private final DepartmentRepository departmentRepository;
	private final UserDepartmentRepository userDepartmentRepository;
	private final UserQueryService userQueryService;

	@Value("${spring.cloud.aws.s3.prefix}")
	private String S3Prefix;

	@Transactional
	public UserResponseDTO.BasicInfoDTO updateBasicInfo(Long userId, UserRequestDTO.BasicInfoDTO req) {
		User user = userQueryService.getUser(userId);
		Job job = jobRepository.getReferenceById(req.jobId());
		user.updateBasicInfo(req.name(), req.nickname(), req.isEntryLevel(), job);

		return new UserResponseDTO.BasicInfoDTO(user.getId(), user.getName(), user.getNickname(), job.getId(),
			user.getIsEntryLevel());
	}

	@Transactional
	public void updateMyInfo(Long userId, UserRequestDTO.MyInfoDTO req) {
		User user = userQueryService.getUser(userId);

		user.updateMyInfo(req.nickname(), req.isEntryLevel(), req.educationLevel());

		if (req.departmentIds() != null && !req.departmentIds().isEmpty()) {
			userDepartmentRepository.deleteAllByUserId(userId);

			List<UserDepartment> newDepartments = req.departmentIds()
				.stream()
				.map(deptId -> UserDepartment.builder()
					.user(user)
					.department(departmentRepository.getReferenceById(deptId))
					.build())
				.toList();

			userDepartmentRepository.saveAll(newDepartments);
		}

	}

	@Transactional
	public void updateProfileImage(Long userId, String imageUrl) {
		String cleanUrl = null;
		if (imageUrl != null) {
			cleanUrl = imageUrl.replaceAll("^\"|\"$", "");
		}
		userQueryService.getUser(userId).updateProfileImage(S3Prefix + cleanUrl);
	}
}
