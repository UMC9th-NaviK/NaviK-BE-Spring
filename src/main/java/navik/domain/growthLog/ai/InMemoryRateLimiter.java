package navik.domain.growthLog.ai;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimiter implements RetryRateLimiter {

	private static final long WINDOW_MILLIS = 60_000L;

	private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

	@Override
	public boolean tryAcquire(String key, int limit) {
		long now = System.currentTimeMillis();
		long cutoff = now - WINDOW_MILLIS;

		Deque<Long> q = buckets.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

		synchronized (q) {
			while (true) {
				Long head = q.peekFirst();
				if (head == null || head >= cutoff)
					break;
				q.pollFirst();
			}

			if (q.size() >= limit) {
				return false;
			}

			q.addLast(now);
			return true;
		}
	}
}
