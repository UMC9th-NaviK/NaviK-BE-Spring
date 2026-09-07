package navik.domain.growthLog.pipeline;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.normalizer.AbilityNormalizer;
import navik.domain.ability.service.AbilityCommandService;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineResultService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final GrowthLogRepository logs;
    private final GrowthLogPersistenceService persistence;
    private final AbilityCommandService abilities;
    private final AbilityNormalizer normalizer;

    @Transactional
    public void apply(String jobId) throws JsonProcessingException {
        var rows = jdbc.queryForList("SELECT *, next_attempt_at > CURRENT_TIMESTAMP AS deferred FROM growth_analysis_job WHERE id = ? FOR UPDATE", jobId);
        if (rows.isEmpty()) return; // retained results have an explicit expiry policy
        var job = rows.getFirst();
        if (job.get("finished_at") != null || Boolean.TRUE.equals(job.get("deferred"))) return;
        Long userId = ((Number) job.get("user_id")).longValue();
        Long logId = ((Number) job.get("growth_log_id")).longValue();
        String token = (String) job.get("processing_token");
        var log = logs.findByIdAndUserId(logId, userId).orElse(null);
        if (log == null || !token.equals(log.getProcessingToken()) || log.getStatus() == GrowthLogStatus.COMPLETED) {
            finish(jobId, "SUPERSEDED");
            return;
        }
        if ("FAILED".equals(job.get("state"))) {
            logs.updateStatusIfMatchAndToken(userId, logId, GrowthLogStatus.PENDING, GrowthLogStatus.FAILED, token);
            logs.updateStatusIfMatchAndToken(userId, logId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED, token);
            finish(jobId, "FAILED");
            return;
        }
        if (!"APPLY".equals(job.get("stage")) || job.get("result_json") == null) {
            throw new IllegalStateException("Result is not ready");
        }
        var result = mapper.readValue((String) job.get("result_json"), GrowthLogEvaluationResult.class);
        if (result.title() == null || result.title().isBlank() || result.content() == null
            || result.content().isBlank() || result.kpis() == null || result.abilities() == null) {
            throw new IllegalArgumentException("Invalid growth analysis result");
        }
        var validAbilities = normalizer.normalize(result.abilities());
        if (validAbilities.size() != result.abilities().size()) {
            throw new IllegalArgumentException("Incomplete ability embeddings");
        }
        logs.updateStatusIfMatchAndToken(userId, logId, GrowthLogStatus.PENDING, GrowthLogStatus.PROCESSING, token);
        if (logs.acquireApplyLock(userId, logId, token) != 1) {
            throw new IllegalStateException("Cannot acquire result application ownership");
        }
        int total = 0;
        java.util.Set<Long> seenKpis = new java.util.HashSet<>();
        for (var kpi : result.kpis()) {
            if (kpi == null || kpi.kpiCardId() == null || kpi.delta() == null || kpi.delta() < 0 || kpi.delta() > 15 || !seenKpis.add(kpi.kpiCardId())) {
                throw new IllegalArgumentException("Invalid KPI result");
            }
            total = Math.addExact(total, kpi.delta());
        }
        // Existing best-effort persistence receives no embeddings. Strict embedding write joins this transaction.
        var withoutAbilities = new GrowthLogEvaluationResult(result.title(), result.content(), result.kpis(), java.util.List.of());
        persistence.completeGrowthLogAfterProcessing(userId, logId, withoutAbilities, total);
        abilities.saveAbilitiesAtomically(userId, validAbilities);
        logs.clearProcessingTokenIfMatch(userId, logId, token, GrowthLogStatus.COMPLETED);
        finish(jobId, "COMPLETED");
    }

    @Transactional
    public void retryOrFail(String jobId) {
        var rows = jdbc.queryForList("SELECT *, next_attempt_at > CURRENT_TIMESTAMP AS deferred FROM growth_analysis_job WHERE id = ? FOR UPDATE", jobId);
        if (rows.isEmpty() || rows.getFirst().get("finished_at") != null) return;
        var job = rows.getFirst();
        int attempt = ((Number) job.get("attempt")).intValue() + 1;
        if (attempt >= 3) {
            Long userId = ((Number) job.get("user_id")).longValue();
            Long logId = ((Number) job.get("growth_log_id")).longValue();
            String token = (String) job.get("processing_token");
            logs.updateStatusIfMatchAndToken(userId, logId, GrowthLogStatus.PENDING, GrowthLogStatus.FAILED, token);
            logs.updateStatusIfMatchAndToken(userId, logId, GrowthLogStatus.PROCESSING, GrowthLogStatus.FAILED, token);
            jdbc.update("UPDATE growth_analysis_job SET attempt = ?, error_code = 'APPLY_FAILED' WHERE id = ?", attempt, jobId);
            finish(jobId, "FAILED");
            jdbc.update("INSERT INTO growth_analysis_outbox(id, job_id, stage) VALUES (?, ?, 'DLQ')",
                java.util.UUID.randomUUID().toString(), jobId);
        } else {
            jdbc.update("UPDATE growth_analysis_job SET attempt = ?, error_code = 'APPLY_RETRY', next_attempt_at = CURRENT_TIMESTAMP + INTERVAL '30 seconds', updated_at = CURRENT_TIMESTAMP WHERE id = ?", attempt, jobId);
            jdbc.update("""
                INSERT INTO growth_analysis_outbox(id, job_id, stage, available_at)
                VALUES (?, ?, 'APPLY', CURRENT_TIMESTAMP + INTERVAL '30 seconds')
                """, java.util.UUID.randomUUID().toString(), jobId);
        }
    }

    private void finish(String id, String state) {
        jdbc.update("""
            UPDATE growth_analysis_job SET state = ?, finished_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """, state, id);
    }
}
