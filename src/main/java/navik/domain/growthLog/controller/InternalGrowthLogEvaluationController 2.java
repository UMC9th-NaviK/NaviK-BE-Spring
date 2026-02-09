package navik.domain.growthLog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;
import navik.domain.growthLog.service.command.GrowthLogEvaluationApplyService;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/growth-logs")
@Validated
public class InternalGrowthLogEvaluationController {

	private final GrowthLogEvaluationApplyService applyService;

	@PostMapping("/{growthLogId}/processing-start")
	public ResponseEntity<Void> processingStart(
		@PathVariable Long growthLogId,
		@Valid @RequestBody GrowthLogInternalProcessingStartRequest req
	) {
		applyService.startProcessing(growthLogId, req);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{growthLogId}/evaluation-result")
	public ResponseEntity<Void> applyResult(
		@PathVariable Long growthLogId,
		@Valid @RequestBody GrowthLogInternalApplyEvaluationRequest req
	) {
		applyService.applyResult(growthLogId, req);
		return ResponseEntity.ok().build();
	}
}
