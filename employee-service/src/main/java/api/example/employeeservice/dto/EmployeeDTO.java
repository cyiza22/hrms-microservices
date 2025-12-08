package api.example.employeeservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String designation;
    private String employeeType;
    private LocalDate joiningDate;
    private String status;
    private Long departmentId;
    private String departmentName;
}
