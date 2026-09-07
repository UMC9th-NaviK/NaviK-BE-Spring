package navik.pipeline.e2e;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import navik.domain.growthLog.pipeline.*;
import navik.domain.growthLog.service.command.*;
import navik.domain.growthLog.service.command.strategy.AsyncGrowthLogEvaluationStrategy;
import navik.domain.growthLog.service.query.*;
import navik.domain.growthLog.worker.*;
import navik.domain.growthLog.message.RedisStreamGrowthLogEvaluationPublisher;
import navik.domain.growthLog.controller.GrowthLogController;
import navik.domain.growthLog.ai.client.*;
import navik.domain.growthLog.ai.limiter.InMemoryRateLimiter;
import navik.domain.ability.service.AbilityCommandService;
import navik.domain.ability.normalizer.AbilityNormalizer;
import navik.domain.kpi.service.command.KpiScoreIncrementService;
import navik.global.auth.handler.AuthUserArgumentResolver;
import navik.global.auth.jwt.*;
import navik.global.config.SchedulerConfig;

/** Test-classpath-only bootstrap: production growth components, real JWT/JPA/Redis, no unrelated integrations. */
@org.springframework.boot.test.context.TestConfiguration
@EnableAutoConfiguration
@EntityScan("navik.domain")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages={"navik.domain.users.repository", "navik.domain.growthLog.repository",
    "navik.domain.kpi.repository", "navik.domain.ability.repository", "navik.domain.portfolio.repository"})
@Import({GrowthLogController.class, GrowthLogEvaluationService.class, GrowthLogEvaluationCoreService.class,
    GrowthLogPersistenceService.class, GrowthLogQueryService.class, GrowthLogAggregateService.class,
    AbilityCommandService.class, AbilityNormalizer.class, KpiScoreIncrementService.class, InMemoryRateLimiter.class,
    AsyncGrowthLogEvaluationStrategy.class, RedisStreamGrowthLogEvaluationPublisher.class,
    RedisStreamGrowthLogEvaluationWorker.class, GrowthLogEvaluationWorkerProcessor.class,
    StreamGrowthLogEvaluationStrategy.class, GrowthPipelineJobs.class, GrowthPipelineOutboxRelay.class,
    GrowthPipelineResultService.class, GrowthPipelineConfiguration.class, GrowthPipelineMaintenance.class,
    navik.global.apiPayload.exception.handler.ExceptionAdvice.class, GrowthPipelineStatusController.class, JwtTokenProvider.class, AuthUserArgumentResolver.class, SchedulerConfig.class})
public class GrowthApiHarness {
    public static void main(String[] args) {
        if (!"true".equals(System.getenv("PIPELINE_E2E_ENABLED")))
            throw new IllegalStateException("Explicit isolated E2E opt-in required");
        SpringApplication.run(GrowthApiHarness.class, args);
    }
    @Bean GrowthLogAiClient aiClient() {
        return new AiServerGrowthLogAiClient(WebClient.builder().baseUrl("http://127.0.0.1:58082").build());
    }
    @Bean SecurityFilterChain harnessSecurity(HttpSecurity http, JwtTokenProvider tokens) throws Exception {
        return http.csrf(c -> c.disable()).sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(c -> c.anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(tokens), UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean WebMvcConfigurer harnessMvc(AuthUserArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) { resolvers.add(resolver); }
        };
    }
}
