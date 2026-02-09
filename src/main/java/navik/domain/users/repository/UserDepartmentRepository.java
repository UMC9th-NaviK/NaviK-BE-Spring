package navik.domain.users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import navik.domain.users.entity.UserDepartment;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {

	@Query("SELECT ud.department.id FROM UserDepartment ud WHERE ud.user.id = :userId")
	List<Long> findDepartmentIdsByUserId(Long userId);
	void deleteAllByUserId(Long userId);
}