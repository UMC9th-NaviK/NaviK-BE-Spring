package navik.domain.growthLog.ai.limiter;

public interface RetryRateLimiter {
	boolean tryAcquire(String key, int limit);
}
