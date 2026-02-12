package navik.domain.portfolio.worker;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.event.KpiScoreUpdatedEvent;
import navik.domain.kpi.service.command.KpiScoreInitialService;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.repository.PortfolioRepository;
import navik.domain.users.enums.UserStatus;
import navik.domain.users.exception.code.JobErrorCode;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioAnalysisWorkerProcessor {

	private final PortfolioRepository portfolioRepository;
	private final PortfolioAiClient portfolioAiClient;
	private final KpiScoreInitialService kpiScoreInitialService;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final UserQueryService userQueryService;

	@Transactional
	public boolean process(Long userId, Long portfolioId, String traceId, boolean isFallBacked) {
		// 1) 포트폴리오 조회
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio == null) {
			log.warn("[PortfolioAnalysis] skip (not found). traceId={}, portfolioId={}", traceId, portfolioId);
			return false;
		}
		portfolio.updateStatus(PortfolioStatus.PROCESSING);

		// 2) 분석 요청 및 KPI 점수 초기화
		boolean success = isFallBacked ? processFallbackAnalysis(userId, portfolio, traceId) :
			processNormalAnalysis(userId, portfolio, portfolioId, traceId);
		if (!success) {
			return false;
		}

		eventPublisher.publishEvent(new KpiScoreUpdatedEvent(userId));

		if (portfolio.getStatus() != PortfolioStatus.RETRY_REQUIRED) {
			portfolio.updateStatus(PortfolioStatus.COMPLETED);
			userQueryService.getUser(userId).updateUserStatus(UserStatus.ACTIVE);
		}

		return true;
	}

	/**
	 * 일반 분석: basis="none"인 항목은 score=0으로 강제하여 초기화
	 * none이 있으면 RETRY_REQUIRED 상태로 마킹
	 */
	private boolean processNormalAnalysis(Long userId, Portfolio portfolio, Long portfolioId, String traceId) {
		PortfolioAiDTO.AnalyzeResponse result = analyzePortfolio(userId, portfolioId, traceId, portfolio);

		if (isEmptyResponse(result)) {
			log.warn("[PortfolioAnalysis] skip (empty AI response). traceId={}, portfolioId={}", traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.FAILED);
			return false;
		}

		boolean hasNoneBasis = result.scores().stream().anyMatch(s -> "none".equals(s.basis()));

		// basis="none"인 항목은 score를 0으로 강제
		List<KpiScoreRequestDTO.Item> items = result.scores()
			.stream()
			.map(s -> new KpiScoreRequestDTO.Item(s.kpiId(), "none".equals(s.basis()) ? 0 : s.score()))
			.toList();

		kpiScoreInitialService.initializeKpiScores(userId, new KpiScoreRequestDTO.Initialize(items));

		if (hasNoneBasis) {
			log.warn("[PortfolioAnalysis] `basis=none` detected. Try fallback logic. traceId={}, portfolioId={}",
				traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.RETRY_REQUIRED);
			return true; // 비즈니스 로직상 재시도 필요하지만 워커 기준 completed
		}

		log.info("[PortfolioAnalysis] completed. traceId={}, userId={}, portfolioId={}, scoreCount={}", traceId, userId,
			portfolioId, result.scores().size());
		return true;
	}

	/**
	 * Fallback 분석: 기존 score가 0인 항목만 업데이트
	 */
	private boolean processFallbackAnalysis(Long userId, Portfolio portfolio, String traceId) {
		PortfolioAiDTO.AnalyzeResponse result = reanalyzePortfolio(userId, portfolio);

		if (isEmptyResponse(result)) {
			log.warn("[PortfolioAnalysis] skip (empty fallback response). traceId={}, portfolioId={}", traceId,
				portfolio.getId());
			portfolio.updateStatus(PortfolioStatus.FAILED);
			return false;
		}

		List<KpiScoreRequestDTO.Item> items = toScoreItems(result.scores());
		kpiScoreInitialService.updateZeroScoresOnly(userId, new KpiScoreRequestDTO.Initialize(items));

		log.info("[PortfolioAnalysis] fallback completed. traceId={}, userId={}, portfolioId={}, scoreCount={}",
			traceId, userId, portfolio.getId(), result.scores().size());

		userQueryService.getUser(userId).updateUserStatus(UserStatus.ACTIVE);
		return true;
	}

	private PortfolioAiDTO.AnalyzeResponse analyzePortfolio(Long userId, Long portfolioId, String traceId,
		Portfolio portfolio) {
		String resumeText = portfolio.getContent();
		if (resumeText == null || resumeText.isBlank()) {
			log.warn("[PortfolioAnalysis] skip (empty content). traceId={}, portfolioId={}", traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.FAILED);
			return null;
		}
		return portfolioAiClient.analyzePortfolio(resumeText, getJobId(userId));
	}

	private PortfolioAiDTO.AnalyzeResponse reanalyzePortfolio(Long userId, Portfolio portfolio) {
		return portfolioAiClient.analyzeWithFallback(getJobId(userId), portfolio.getQB1(), portfolio.getQB2(),
			portfolio.getQB3(), portfolio.getQB4(), portfolio.getQB5());
	}

	private boolean isEmptyResponse(PortfolioAiDTO.AnalyzeResponse result) {
		return result == null || result.scores() == null || result.scores().isEmpty();
	}

	private List<KpiScoreRequestDTO.Item> toScoreItems(List<PortfolioAiDTO.AnalyzeResponse.KpiScoreItem> scores) {
		return scores.stream().map(s -> new KpiScoreRequestDTO.Item(s.kpiId(), s.score())).toList();
	}

	private Long getJobId(Long userId) {
		return userRepository.findJobIdByUserId(userId)
			.orElseThrow(() -> new GeneralException(JobErrorCode.JOB_NOT_FOUND));
	}
}
