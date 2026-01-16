package navik.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.users.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
