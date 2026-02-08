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
import navik.domain.users.exception.code.JobErrorCode;
import navik.domain.users.repository.UserRepository;
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

	@Transactional
	public boolean process(Long userId, Long portfolioId, String traceId, boolean isFallBacked) {
		// 1) 포트폴리오 조회
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio == null) {
			log.warn("[PortfolioAnalysis] skip (not found). traceId={}, portfolioId={}", traceId, portfolioId);
			return false;
		} else {
			portfolio.updateStatus(PortfolioStatus.PROCESSING);
		}

		// 2) AI 서버 분석 요청
		PortfolioAiDTO.AnalyzeResponse result = isFallBacked ? reanalyzePortfolio(userId, portfolio) : analyzePortfolio(userId, portfolioId, traceId, portfolio);

		if (result == null || result.scores() == null || result.scores().isEmpty()) {
			log.warn("[PortfolioAnalysis] skip (empty AI response). traceId={}, portfolioId={}", traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.FAILED);
			return false;
		}

		// 3) KPI 점수 초기화
		List<KpiScoreRequestDTO.Item> items = result.scores()
			.stream()
			.map(s -> new KpiScoreRequestDTO.Item(s.kpiId(), s.score()))
			.toList();

		kpiScoreInitialService.initializeKpiScores(userId, new KpiScoreRequestDTO.Initialize(items));

		portfolio.updateStatus(PortfolioStatus.COMPLETED);
		log.info("[PortfolioAnalysis] completed. traceId={}, userId={}, portfolioId={}, scoreCount={}", traceId, userId,
			portfolioId, result.scores().size());

		eventPublisher.publishEvent(new KpiScoreUpdatedEvent(userId));

		return true;
	}

	private PortfolioAiDTO.AnalyzeResponse reanalyzePortfolio(Long userId, Portfolio portfolio) {
		PortfolioAiDTO.AnalyzeResponse result;
		result = portfolioAiClient.analyzeWithFallback(getJobId(userId), portfolio.getQB1(), portfolio.getQB2(),
			portfolio.getQB3(), portfolio.getQB4(), portfolio.getQB5());
		return result;
	}

	private PortfolioAiDTO.AnalyzeResponse analyzePortfolio(Long userId, Long portfolioId, String traceId,
		Portfolio portfolio) {
		PortfolioAiDTO.AnalyzeResponse result;
		String resumeText = portfolio.getContent();
		if (resumeText == null || resumeText.isBlank()) {
			log.warn("[PortfolioAnalysis] skip (empty content). traceId={}, portfolioId={}", traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.FAILED);
			return null;
		}
		result = portfolioAiClient.analyzePortfolio(resumeText, getJobId(userId));

		if(checkScoreContainsNoneValueInBasis(result)) {
			log.warn("[PortfolioAnalysis] Need to try fallback request  (AI response contains none value in basis). traceId={}, portfolioId={}",
				traceId, portfolioId);
			portfolio.updateStatus(PortfolioStatus.FAILED);
		}

		return result;
	}

	private boolean checkScoreContainsNoneValueInBasis(PortfolioAiDTO.AnalyzeResponse result) {
		return result.scores().stream().anyMatch(s -> "none".equals(s.basis()));
	}

	private Long getJobId(Long userId) {
		return userRepository.findJobIdByUserId(userId)
			.orElseThrow(() -> new GeneralException(JobErrorCode.JOB_NOT_FOUND));
	}
}
