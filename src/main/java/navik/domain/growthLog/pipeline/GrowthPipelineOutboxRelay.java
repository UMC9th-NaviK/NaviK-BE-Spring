package navik.domain.growthLog.pipeline;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "navik.growth-log.pipeline.enabled", havingValue = "true")
public class GrowthPipelineOutboxRelay {
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;

    public static String stream(String stage) {
        return switch (stage) {
            case "ANALYZE" -> "{growth-v2}:analyze";
            case "EMBED" -> "{growth-v2}:embed";
            case "APPLY" -> "{growth-v2}:results";
            case "DLQ" -> "{growth-v2}:dead";
            default -> throw new IllegalArgumentException("Unknown pipeline stage");
        };
    }

    // A successful XADD followed by rollback can be published again. eventId is stable.
    @Scheduled(fixedDelayString = "${navik.growth-log.pipeline.outbox-poll-ms:500}")
    @Transactional
    public void publishDue() {
        var events = jdbc.queryForList("""
            SELECT id, job_id, stage FROM growth_analysis_outbox
            WHERE published_at IS NULL AND available_at <= CURRENT_TIMESTAMP
            ORDER BY available_at, id LIMIT 20 FOR UPDATE SKIP LOCKED
            """);
        for (var event : events) {
            var record = redis.opsForStream().add(MapRecord.create(stream((String) event.get("stage")),
                Map.of("schemaVersion", "1", "eventId", (String) event.get("id"),
                    "jobId", (String) event.get("job_id"), "stage", (String) event.get("stage"))));
            if (record == null) {
                throw new IllegalStateException("Redis did not confirm the outbox event");
            }
            jdbc.update("UPDATE growth_analysis_outbox SET published_at = CURRENT_TIMESTAMP WHERE id = ?",
                event.get("id"));
        }
    }
}
