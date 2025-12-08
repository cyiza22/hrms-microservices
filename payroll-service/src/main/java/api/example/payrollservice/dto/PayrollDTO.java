package api.example.payrollservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;
    private String employeeEmail;

    @NotNull(message = "CTC is required")
    @DecimalMin(value = "0.0", message = "CTC must be positive")
    private BigDecimal ctc;

    @NotNull(message = "Salary per month is required")
    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal salaryPerMonth;

    @DecimalMin(value = "0.0", message = "Deduction must be non-negative")
    private BigDecimal deduction;

    @NotNull(message = "Status is required")
    private String status;

    private BigDecimal netSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
