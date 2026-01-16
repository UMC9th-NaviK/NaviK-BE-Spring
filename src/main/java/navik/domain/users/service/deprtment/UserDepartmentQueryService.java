package navik.domain.users.service.deprtment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.entity.Department;
import navik.domain.users.entity.UserDepartment;
import navik.domain.users.repository.UserDepartmentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDepartmentQueryService {
	private final UserDepartmentRepository userDepartmentRepository;

	public List<String> getUserDepartments(Long userId){
		return userDepartmentRepository.findByUserId(userId).stream()
			.map(UserDepartment::getDepartment)
			.map(Department::getName)
			.toList();
	}
}
