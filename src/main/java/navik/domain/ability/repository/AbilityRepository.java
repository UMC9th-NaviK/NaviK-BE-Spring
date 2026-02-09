package navik.domain.ability.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import navik.domain.ability.entity.Ability;
import navik.domain.users.entity.User;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, Long> {

	@Query("""
		SELECT a FROM Ability a 
		WHERE a.user = :user 
		  AND (a.createdAt < :lastCreatedAt 
		       OR (a.createdAt = :lastCreatedAt AND a.id < :lastId)) 
		ORDER BY a.createdAt DESC, a.id DESC
		""")
	Slice<Ability> findByUserWithCursor(
		@Param("user") User user,
		@Param("lastCreatedAt") LocalDateTime lastCreatedAt,
		@Param("lastId") Long lastId,
		Pageable pageable
	);

	Slice<Ability> findByUserOrderByCreatedAtDescIdDesc(User user, Pageable pageable);
}
