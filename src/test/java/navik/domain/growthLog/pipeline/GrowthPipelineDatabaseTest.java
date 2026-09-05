package navik.domain.growthLog.pipeline;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import navik.domain.ability.normalizer.AbilityNormalizer;
import navik.domain.ability.service.AbilityCommandService;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.growthLog.service.command.GrowthLogPersistenceService;

class GrowthPipelineDatabaseTest {
    private JdbcTemplate jdbc;
    private TransactionTemplate tx;
    private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private GrowthLogRepository logs;
    private GrowthLogPersistenceService persistence;
    private AbilityCommandService abilities;
    private GrowthPipelineResultService results;
    private String id;
    private String token;
    private boolean postgres;

    @BeforeEach
    void setup() throws Exception {
        String url = System.getenv("PIPELINE_TEST_JDBC_URL");
        postgres = url != null && !url.isBlank();
        DriverManagerDataSource dataSource;
        if (postgres) {
            var admin = new DriverManagerDataSource(url, "pipeline_test", "");
            String schema = "test_" + UUID.randomUUID().toString().replace("-", "");
            new JdbcTemplate(admin).execute("CREATE SCHEMA " + schema);
            dataSource = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema, "pipeline_test", "");
        } else {
            dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        }
        new ResourceDatabasePopulator(new ClassPathResource("db/growth-pipeline.sql")).execute(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        tx = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        jdbc.execute("CREATE TABLE application_ledger (id integer)");
        logs = mock(GrowthLogRepository.class);
        persistence = mock(GrowthLogPersistenceService.class);
        abilities = mock(AbilityCommandService.class);
        results = new GrowthPipelineResultService(jdbc, mapper, logs, persistence, abilities, new AbilityNormalizer());
        token = UUID.randomUUID().toString(); id = UUID.randomUUID().toString();
        var log = mock(GrowthLog.class);
        when(log.getProcessingToken()).thenReturn(token);
        when(log.getStatus()).thenReturn(GrowthLogStatus.PENDING);
        when(logs.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(log));
        when(logs.acquireApplyLock(1L, 2L, token)).thenReturn(1);
        doAnswer(call -> { jdbc.update("INSERT INTO application_ledger(id) VALUES (1)"); return null; })
            .when(persistence).completeGrowthLogAfterProcessing(anyLong(), anyLong(), any(), anyInt());
    }

    private void insertResult() throws Exception {
        var result = new GrowthLogEvaluationResult("title", "content",
            List.of(new GrowthLogEvaluationResult.KpiDelta(1L, 3)),
            List.of(new GrowthLogEvaluationResult.AbilityResult("ability", new float[1536])));
        jdbc.update("""
            INSERT INTO growth_analysis_job(id,user_id,growth_log_id,processing_token,input_json,result_json,stage)
            VALUES (?,1,2,?,'{}',?,'APPLY')
            """, id, token, mapper.writeValueAsString(result));
    }

    private void apply() {
        tx.executeWithoutResult(status -> {
            try { results.apply(id); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void duplicateCompletionDoesNotApplyBusinessEffectsTwice() throws Exception {
        insertResult(); apply(); apply();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM application_ledger", Long.class)).isEqualTo(1);
        verify(abilities, times(1)).saveAbilitiesAtomically(eq(1L), anyList());
        assertThat(jdbc.queryForObject("SELECT state FROM growth_analysis_job WHERE id=?", String.class, id)).isEqualTo("COMPLETED");
    }

    @Test
    void vectorFailureRollsBackBusinessEffectAndCompletionBeforeRetry() throws Exception {
        insertResult();
        doThrow(new RuntimeException("database failure")).doNothing().when(abilities).saveAbilitiesAtomically(anyLong(), anyList());
        assertThatThrownBy(this::apply).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM application_ledger", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT finished_at FROM growth_analysis_job WHERE id=?", Object.class, id)).isNull();
        apply();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM application_ledger", Long.class)).isEqualTo(1);
    }

    @Test
    void oldProcessingTokenNeverOverwritesCurrentJob() throws Exception {
        insertResult();
        var current = mock(GrowthLog.class);
        when(current.getProcessingToken()).thenReturn("new-token");
        when(logs.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(current));
        apply();
        verifyNoInteractions(persistence, abilities);
        assertThat(jdbc.queryForObject("SELECT state FROM growth_analysis_job WHERE id=?", String.class, id)).isEqualTo("SUPERSEDED");
    }

    @Test
    void outboxFailureLeavesEventUnpublishedAndRelayCanRetry() throws Exception {
        insertResult();
        jdbc.update("INSERT INTO growth_analysis_outbox(id,job_id,stage) VALUES (?,?,'APPLY')", UUID.randomUUID().toString(), id);
        var redis = mock(StringRedisTemplate.class);
        StreamOperations<String, Object, Object> streams = mock(StreamOperations.class);
        when(redis.opsForStream()).thenReturn(streams);
        when(streams.add(any(org.springframework.data.redis.connection.stream.MapRecord.class)))
            .thenThrow(new RuntimeException("redis unavailable"))
            .thenReturn(org.springframework.data.redis.connection.stream.RecordId.of("1-0"));
        var relay = new GrowthPipelineOutboxRelay(jdbc, redis);
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> relay.publishDue())).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject("SELECT published_at FROM growth_analysis_outbox", Object.class)).isNull();
        tx.executeWithoutResult(status -> relay.publishDue());
        assertThat(jdbc.queryForObject("SELECT published_at FROM growth_analysis_outbox", Object.class)).isNotNull();
    }

    @Test
    void admissionAndEnqueueParticipateInTheCreationTransaction() {
        assumeTrue(postgres, "PostgreSQL advisory transaction lock test requires PIPELINE_TEST_JDBC_URL");
        var jobs = new GrowthPipelineJobs(jdbc, mapper);
        ReflectionTestUtils.setField(jobs, "maxOutstandingJobs", 1);
        var input = new GrowthLogAiRequestDTO.EvaluateUserInputRequest(1L, 1L, 1,
            new GrowthLogAiRequestDTO.GrowthLogEvaluationContext("resume", List.of(), List.of(), "input"));
        tx.executeWithoutResult(status -> { jobs.enqueue(1L, 2L, token, input); status.setRollbackOnly(); });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM growth_analysis_job", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM growth_analysis_outbox", Long.class)).isZero();
        tx.executeWithoutResult(status -> jobs.enqueue(1L, 2L, token, input));
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> jobs.enqueue(1L, 3L, UUID.randomUUID().toString(), input)))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void concurrentCompletionsSerializeOnTheJobRow() throws Exception {
        insertResult();
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var start = new java.util.concurrent.CountDownLatch(1);
            var first = executor.submit(() -> { start.await(); apply(); return true; });
            var second = executor.submit(() -> { start.await(); apply(); return true; });
            start.countDown();
            assertThat(first.get(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(second.get(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM application_ledger", Long.class)).isEqualTo(1);
        verify(abilities, times(1)).saveAbilitiesAtomically(eq(1L), anyList());
    }

}
