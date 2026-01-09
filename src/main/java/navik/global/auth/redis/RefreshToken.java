package navik.global.auth.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14) // 14일
public class RefreshToken {

	@Id
	private String id; // User ID (PK)

	@Indexed
	private String token;

	public void updateToken(String token) {
		this.token = token;
	}
}
