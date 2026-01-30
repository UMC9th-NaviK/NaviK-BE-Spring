package navik.domain.users.dto;

import java.util.List;

public class DepartmentResponseDTO {

	public record DepartmentItem(Long id, String name) {}

	public record DepartmentList(
		List<DepartmentItem> departmentItemList
	) {}
}


