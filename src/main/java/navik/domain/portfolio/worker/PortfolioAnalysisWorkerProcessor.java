package navik.domain.portfolio.worker;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.service.command.KpiScoreInitialService;
import navik.domain.portfolio.ai.client.PortfolioAiClient;
import navik.domain.portfolio.dto.PortfolioAiDTO;
import navik.domain.portfolio.entity.Portfolio;
import navik.domain.portfolio.entity.PortfolioStatus;
import navik.domain.portfolio.repository.PortfolioRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioAnalysisWorkerProcessor {

	private final PortfolioRepository portfolioRepository;
	private final PortfolioAiClient portfolioAiClient;
	private final KpiScoreInitialService kpiScoreInitialService;

	@Transactional
	public boolean process(Long userId, Long portfolioId, String traceId) {

		// 1) 포트폴리오 조회
		Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
		if (portfolio == null) {
			log.warn("[PortfolioAnalysis] skip (not found). traceId={}, portfolioId={}", traceId, portfolioId);
			return false;
		}else{
			portfolio.updateStatus(PortfolioStatus.PROCESSING);
		}

		// 2) AI 서버 분석 요청 (추가 정보 유무에 따라 분기)
		PortfolioAiDTO.AnalyzeResponse result;

		if (hasAdditionalInfo(portfolio)) {
			// Fallback 요청 (추가 정보 있음)
			result = portfolioAiClient.analyzeWithFallback(
				"backend",
				portfolio.getQB1(),
				portfolio.getQB2(),
				portfolio.getQB3(),
				portfolio.getQB4(),
				portfolio.getQB5()
			);
		} else {
			// 일반 분석 요청
			String resumeText = portfolio.getContent();
			if (resumeText == null || resumeText.isBlank()) {
				log.warn("[PortfolioAnalysis] skip (empty content). traceId={}, portfolioId={}", traceId, portfolioId);
				portfolio.updateStatus(PortfolioStatus.FAILED);
				return false;
			}
			result = portfolioAiClient.analyzePortfolio(resumeText);
		}

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

		return true;
	}

	private boolean hasAdditionalInfo(Portfolio portfolio) {
		return portfolio.getQB1() != null
			&& portfolio.getQB2() != null
			&& portfolio.getQB3() != null
			&& portfolio.getQB4() != null
			&& portfolio.getQB5() != null;
	}
}
