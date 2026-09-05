package navik.domain.growthLog.pipeline;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;
import navik.pipeline.PipelineStreamConsumer;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineConfiguration {
    @Bean(initMethod = "start", destroyMethod = "close")
    public PipelineStreamConsumer growthResultConsumer(StringRedisTemplate redis, GrowthPipelineResultService results,
        org.springframework.jdbc.core.JdbcTemplate jdbc) {
        jdbc.queryForList("SELECT id, next_attempt_at, embedding_model FROM growth_analysis_job WHERE 1=0");
        return new PipelineStreamConsumer(redis, "{growth-v2}:results", "growth-results-v2", 2,
            Duration.ofSeconds(120), id -> {
                try { results.apply(id); }
                catch (Exception e) {
                    log.warn("Growth result application failed: jobId={}, error={}", id, e.getClass().getSimpleName());
                    // This runs after apply's transaction has rolled back. Failure to persist retry leaves PEL intact.
                    results.retryOrFail(id);
                }
                return true;
            });
    }
}
