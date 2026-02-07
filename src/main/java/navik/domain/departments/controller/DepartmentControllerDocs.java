package navik.domain.departments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.users.dto.DepartmentResponseDTO;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Department", description = "학과 관련 API")
public interface DepartmentControllerDocs {

	@Operation(summary = "학과 목록", description = "전체 학과번호, 학과명을 조회합니다")
	ApiResponse<DepartmentResponseDTO.DepartmentList> getAllDepartments();
}
