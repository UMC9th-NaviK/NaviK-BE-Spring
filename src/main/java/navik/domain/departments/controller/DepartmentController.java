package navik.domain.departments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import navik.domain.departments.service.DepartmentQueryService;
import navik.domain.users.dto.DepartmentResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;

@Tag(name = "Department", description = "학과 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/departments")
public class DepartmentController {
	private final DepartmentQueryService departmentQueryService;

	@GetMapping
	@Operation(summary = "학과 목록", description = "전체 학과번호, 학과명을 조회합니다")
	ApiResponse<DepartmentResponseDTO.DepartmentList> getAllDepartments() {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, departmentQueryService.getAllDepartments());
	}
}
