package navik.domain.growthLog.pipeline;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO.EvaluateUserInputRequest;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineJobs {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    @Value("${navik.growth-log.pipeline.max-outstanding-jobs:1000}")
    private int maxOutstandingJobs;

    @Transactional(propagation = Propagation.MANDATORY)
    public String enqueue(Long userId, Long growthLogId, String token, EvaluateUserInputRequest input) {
        // Admission is serialized across API instances until the enclosing creation transaction commits.
        jdbc.queryForList("SELECT pg_advisory_xact_lock(72469211)");
        Long outstanding = jdbc.queryForObject("SELECT count(*) FROM growth_analysis_job WHERE finished_at IS NULL", Long.class);
        if (maxOutstandingJobs < 1 || outstanding >= maxOutstandingJobs) {
            throw new GeneralException(GrowthLogErrorCode.ANALYSIS_QUEUE_FULL);
        }
        String id = UUID.randomUUID().toString();
        try {
            jdbc.update("""
                INSERT INTO growth_analysis_job(id, user_id, growth_log_id, processing_token, input_json)
                VALUES (?, ?, ?, ?, ?)
                """, id, userId, growthLogId, token, mapper.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize growth analysis input", e);
        }
        jdbc.update("INSERT INTO growth_analysis_outbox(id, job_id, stage) VALUES (?, ?, 'ANALYZE')",
            UUID.randomUUID().toString(), id);
        return id;
    }

    public List<Map<String, Object>> status(Long userId, Long growthLogId) {
        return jdbc.queryForList("""
            SELECT id, stage, state, attempt, error_code, created_at, updated_at, finished_at
            FROM growth_analysis_job WHERE user_id = ? AND growth_log_id = ?
            ORDER BY created_at DESC LIMIT 1
            """, userId, growthLogId);
    }
}
