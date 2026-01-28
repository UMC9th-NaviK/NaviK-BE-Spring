package navik.domain.notification.strategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import navik.domain.notification.entity.NotificationType;
import navik.global.apiPayload.exception.code.NotificationErrorCode;
import navik.global.apiPayload.exception.exception.GeneralExceptionHandler;

@Component
public class NotificationMessageStrategyFactory {

	private final Map<NotificationType, NotificationMessageStrategy> strategyMap;

	public NotificationMessageStrategyFactory(List<NotificationMessageStrategy> strategies) {
		this.strategyMap = strategies.stream()
			.collect(Collectors.toMap(NotificationMessageStrategy::getNotificationType, strategy -> strategy));
	}

	public NotificationMessageStrategy getStrategy(NotificationType type) {
		NotificationMessageStrategy strategy = strategyMap.get(type);
		if (strategy == null) {
			throw new GeneralExceptionHandler(NotificationErrorCode.UNSUPPORTED_NOTIFICATION_TYPE);
		}
		return strategy;
	}
}