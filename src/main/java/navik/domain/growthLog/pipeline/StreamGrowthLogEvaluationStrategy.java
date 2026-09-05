package navik.domain.growthLog.pipeline;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.ai.limiter.RetryRateLimiter;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.EvaluateUserInputRequest;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogEvaluationCoreService;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;
import navik.domain.growthLog.service.command.strategy.GrowthLogEvaluationStrategy;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "navik.growth-log.evaluation-mode", havingValue = "stream")
public class StreamGrowthLogEvaluationStrategy implements GrowthLogEvaluationStrategy {
    private final GrowthLogRepository logs;
    private final GrowthLogPersistenceService persistence;
    private final GrowthLogEvaluationCoreService core;
    private final UserRepository users;
    private final GrowthPipelineJobs jobs;
    private final RetryRateLimiter limiter;
    private final EntityManager entityManager;

    @Override
    public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {
        String content = req.content() == null || req.content().isBlank() ? "(내용 없음)" : req.content().trim();
        Long id = persistence.savePendingUserInputLog(userId, content);
        enqueue(userId, id, content);
        return new GrowthLogResponseDTO.CreateResult(id, GrowthLogStatus.PENDING);
    }

    @Override
    public GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId) {
        var log = logs.findByIdAndUserId(growthLogId, userId)
            .orElseThrow(() -> new GeneralException(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));
        if (log.getType() != GrowthType.USER_INPUT) {
            throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_TYPE);
        }
        if (!limiter.tryAcquire("growthLogRetry:" + userId + ":" + growthLogId, 3)) {
            throw new GeneralException(GrowthLogErrorCode.GROWTH_LOG_RETRY_LIMIT_EXCEEDED);
        }
        String content = log.getContent();
        if (logs.updateStatusIfMatch(userId, growthLogId, GrowthLogStatus.FAILED, GrowthLogStatus.PENDING) == 0) {
            throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
        }
        enqueue(userId, growthLogId, content);
        return new GrowthLogResponseDTO.RetryResult(growthLogId, GrowthLogStatus.PENDING);
    }

    private void enqueue(Long userId, Long growthLogId, String content) {
        var user = users.findById(userId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
        var input = new EvaluateUserInputRequest(userId,
            user.getJob() == null ? null : user.getJob().getId(), user.getLevel(), core.buildContext(userId, content));
        String token = UUID.randomUUID().toString();
        entityManager.flush();
        if (logs.prepareStreamJob(userId, growthLogId, token) != 1) {
            throw new GeneralException(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
        }
        jobs.enqueue(userId, growthLogId, token, input);
    }
}
