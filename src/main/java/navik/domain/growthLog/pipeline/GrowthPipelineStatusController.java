package navik.domain.growthLog.pipeline;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineStatusController {
    private final GrowthPipelineJobs jobs;

    @GetMapping("/v1/growth-logs/{growthLogId}/analysis-status")
    public ApiResponse<Map<String, Object>> status(@AuthUser Long userId, @PathVariable Long growthLogId) {
        var status = jobs.status(userId, growthLogId);
        if (status.isEmpty()) throw new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, status.getFirst());
    }
}
