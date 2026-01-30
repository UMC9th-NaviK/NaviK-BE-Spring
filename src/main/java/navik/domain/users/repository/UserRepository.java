package navik.domain.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.users.dto.UserResponseDTO;
import navik.domain.users.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Boolean existsByNickname(String nickname);

	@Query("""
		SELECT new navik.domain.users.dto.UserResponseDTO$UserInfoDTO(
			u.id, u.name, u.email, u.role, u.socialType
		) FROM User u WHERE u.id = :userId
		""")
	Optional<UserResponseDTO.UserInfoDTO> findUserInfoById(@Param("userId") Long userId);

	@Query("""
		SELECT new navik.domain.users.dto.UserResponseDTO$ProfileDTO(
			u.profileImageUrl, u.nickname, u.job.name, u.isEntryLevel
		) FROM User u WHERE u.id = :userId
		""")
	Optional<UserResponseDTO.ProfileDTO> findProfileById(@Param("userId") Long userId);

	@Query("""
		SELECT DISTINCT u FROM User u
		LEFT JOIN FETCH u.userDepartments ud
		LEFT JOIN FETCH ud.department
		WHERE u.id = :userId
		""")
	Optional<User> findByIdWithUserDepartmentAndDepartment(Long userId);

	@Query("SELECT u.id FROM User u")
	List<Long> findAllIds();
}
