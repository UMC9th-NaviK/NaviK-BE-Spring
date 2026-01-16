package navik.domain.users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.users.entity.UserDepartment;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {

	List<UserDepartment> findByUserId(Long userId);
}
