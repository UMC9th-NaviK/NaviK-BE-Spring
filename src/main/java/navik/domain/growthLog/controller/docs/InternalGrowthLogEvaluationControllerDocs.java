package navik.domain.growthLog.controller.docs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.growthLog.dto.internal.GrowthLogInternalApplyEvaluationRequest;
import navik.domain.growthLog.dto.internal.GrowthLogInternalProcessingStartRequest;

@Tag(name = "(백엔드 개발자 내부용 API) Growth Log", description = "(백엔드 개발자 내부용) 성장 로그 API")
public interface InternalGrowthLogEvaluationControllerDocs {

	ResponseEntity<Void> processingStart(
		@PathVariable Long growthLogId,
		@Valid @RequestBody GrowthLogInternalProcessingStartRequest req
	);

	ResponseEntity<Void> applyResult(
		@PathVariable Long growthLogId,
		@Valid @RequestBody GrowthLogInternalApplyEvaluationRequest req
	);
}
