package api.example.employeeservice.factory;

import api.example.employeeservice.entity.Employee;
import api.example.employeeservice.dto.EmployeeDTO;

/**
 * Factory Pattern Interface
 * Purpose: Define contract for creating different types of employees
 */
public interface EmployeeFactory {
    /**
     * Creates an employee based on the DTO
     * @param dto Employee data transfer object
     * @return Configured Employee entity
     */
    Employee createEmployee(EmployeeDTO dto);

    /**
     * Returns the employee type this factory handles
     * @return Employee type (PERMANENT, CONTRACT, etc.)
     */
    String getEmployeeType();
}