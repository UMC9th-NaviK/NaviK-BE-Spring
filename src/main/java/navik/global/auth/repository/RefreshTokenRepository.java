package navik.global.auth.repository;

import org.springframework.data.repository.CrudRepository;

import navik.global.auth.redis.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
