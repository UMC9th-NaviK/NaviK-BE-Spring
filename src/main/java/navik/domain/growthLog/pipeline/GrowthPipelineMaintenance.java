package navik.domain.growthLog.pipeline;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineMaintenance {
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;
    private volatile long activeJobs;
    private volatile long pendingOutbox;
    private volatile long oldestJobSeconds;
    private volatile long failedJobs;
    private final int retentionDays;

    public GrowthPipelineMaintenance(JdbcTemplate jdbc, StringRedisTemplate redis, MeterRegistry registry,
        @Value("${navik.growth-log.pipeline.retention-days:30}") int retentionDays) {
        if (retentionDays < 1) throw new IllegalArgumentException("Retention must be at least one day");
        this.jdbc = jdbc; this.redis = redis; this.retentionDays = retentionDays;
        registry.gauge("growth.pipeline.active.jobs", this, value -> value.activeJobs);
        registry.gauge("growth.pipeline.outbox.pending", this, value -> value.pendingOutbox);
        registry.gauge("growth.pipeline.oldest.job.seconds", this, value -> value.oldestJobSeconds);
        registry.gauge("growth.pipeline.failed.jobs", this, value -> value.failedJobs);
    }

    @Scheduled(fixedDelayString = "${navik.growth-log.pipeline.maintenance-ms:60000}")
    @Transactional
    public void reconcileAndMeasure() {
        // Redis can lose a published notification after a restart. SQL remains the source of job progress.
        var jobs = jdbc.queryForList("""
            SELECT j.id, j.stage, j.state FROM growth_analysis_job j
            WHERE j.finished_at IS NULL AND j.next_attempt_at <= CURRENT_TIMESTAMP
            AND (j.state IN ('READY', 'FAILED') OR (j.state = 'RUNNING' AND j.lease_until < CURRENT_TIMESTAMP))
            AND j.updated_at < CURRENT_TIMESTAMP - INTERVAL '5 minutes'
            AND NOT EXISTS (SELECT 1 FROM growth_analysis_outbox o WHERE o.job_id = j.id
                AND (o.published_at IS NULL OR o.created_at > CURRENT_TIMESTAMP - INTERVAL '5 minutes'))
            LIMIT 100 FOR UPDATE SKIP LOCKED
            """);
        for (var job : jobs) {
            jdbc.update("INSERT INTO growth_analysis_outbox(id, job_id, stage) VALUES (?, ?, ?)",
                UUID.randomUUID().toString(), job.get("id"),
                "FAILED".equals(job.get("state")) ? "APPLY" : job.get("stage"));
        }
        activeJobs = jdbc.queryForObject("SELECT count(*) FROM growth_analysis_job WHERE finished_at IS NULL", Long.class);
        pendingOutbox = jdbc.queryForObject("SELECT count(*) FROM growth_analysis_outbox WHERE published_at IS NULL", Long.class);
        failedJobs = jdbc.queryForObject("SELECT count(*) FROM growth_analysis_job WHERE state = 'FAILED'", Long.class);
        oldestJobSeconds = jdbc.queryForObject("""
            SELECT COALESCE(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - min(created_at))), 0)::bigint
            FROM growth_analysis_job WHERE finished_at IS NULL
            """, Long.class);
    }

    @Scheduled(fixedDelayString = "${navik.growth-log.pipeline.cleanup-ms:3600000}")
    @Transactional
    public void cleanup() {
        Timestamp cutoff = Timestamp.from(Instant.now().minus(Duration.ofDays(retentionDays)));
        // Only terminal jobs with no outstanding outbox work may be removed.
        jdbc.update("""
            DELETE FROM growth_analysis_outbox WHERE job_id IN (
                SELECT j.id FROM growth_analysis_job j WHERE j.finished_at < ?
                AND NOT EXISTS (SELECT 1 FROM growth_analysis_outbox o WHERE o.job_id = j.id AND o.published_at IS NULL))
            """, cutoff);
        jdbc.update("""
            DELETE FROM growth_analysis_job j WHERE j.finished_at < ?
            AND NOT EXISTS (SELECT 1 FROM growth_analysis_outbox o WHERE o.job_id = j.id)
            """, cutoff);
        String minId = cutoff.getTime() + "-0";
        redis.execute((RedisCallback<Object>) connection -> connection.execute("XTRIM",
            bytes("{growth-v2}:dead"), bytes("MINID"), bytes(minId)));
    }

    private byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }
}
