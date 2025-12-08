package api.example.employeeservice.repository;

import api.example.employeeservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);
    List<Employee> findByDepartmentId(Long departmentId);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
}