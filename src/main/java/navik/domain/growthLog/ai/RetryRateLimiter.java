package navik.domain.growthLog.ai;

public interface RetryRateLimiter {
	boolean tryAcquire(String key, int limit);
}
