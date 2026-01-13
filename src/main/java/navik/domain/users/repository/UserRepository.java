package navik.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.users.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Boolean existsByNickname(String nickname);
}
