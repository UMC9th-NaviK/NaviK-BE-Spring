package navik.domain.portfolio.event;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.portfolio.message.PortfolioAnalysisMessage;
import navik.domain.portfolio.message.PortfolioAnalysisPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioAnalysisEventListener {

	private final PortfolioAnalysisPublisher portfolioAnalysisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePortfolioAnalysisEvent(PortfolioAnalysisEvent event) {
		try {
			String traceId = UUID.randomUUID().toString();
			portfolioAnalysisPublisher.publish(
				new PortfolioAnalysisMessage(event.userId(), event.portfolioId(), traceId, event.isFallBacked(),
					event.analysisType())
			);
		} catch (Exception e) {
			log.error("[PortfolioAnalysisEventListener] 분석 메시지 발행 실패. userId={}, portfolioId={}",
				event.userId(), event.portfolioId(), e);
		}
	}
}
