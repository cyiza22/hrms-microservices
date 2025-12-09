package api.example.employeeservice.factory;

import api.example.employeeservice.entity.Employee;
import api.example.employeeservice.dto.EmployeeDTO;
import org.springframework.stereotype.Component;

/**
 * Concrete Factory for Permanent Employees
 * Implements specific creation logic for permanent employees with full benefits
 */
@Component
public class PermanentEmployeeFactory implements EmployeeFactory {

    @Override
    public Employee createEmployee(EmployeeDTO dto) {
        return Employee.builder()
                .employeeId(dto.getEmployeeId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .designation(dto.getDesignation())
                .employeeType("PERMANENT")
                .joiningDate(dto.getJoiningDate())
                .status("ACTIVE")
                .build();
    }

    @Override
    public String getEmployeeType() {
        return "PERMANENT";
    }
}