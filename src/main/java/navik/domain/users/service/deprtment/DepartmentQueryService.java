package navik.domain.users.service.deprtment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.dto.DepartmentResponseDTO;
import navik.domain.users.repository.DepartmentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentQueryService {
	private final DepartmentRepository departmentRepository;

	public DepartmentResponseDTO.DepartmentList getAllDepartments() {

		List<DepartmentResponseDTO.DepartmentItem> items =
			departmentRepository.findAll().stream()
				.map(department ->
					new DepartmentResponseDTO.DepartmentItem(
						department.getId(),
						department.getName()
					)
				)
				.toList();

		return new DepartmentResponseDTO.DepartmentList(items);
	}
}
