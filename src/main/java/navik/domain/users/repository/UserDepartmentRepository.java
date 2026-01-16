package navik.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.users.entity.UserDepartment;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long>{
}
