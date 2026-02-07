package navik.domain.departments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.departments.service.DepartmentQueryService;
import navik.domain.users.dto.DepartmentResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/departments")
public class DepartmentController implements DepartmentControllerDocs {
	private final DepartmentQueryService departmentQueryService;

	@GetMapping
	public ApiResponse<DepartmentResponseDTO.DepartmentList> getAllDepartments() {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, departmentQueryService.getAllDepartments());
	}
}
