package api.example.payrollservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId; // Reference to employee service

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ctc;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryPerMonth;

    @Column(precision = 12, scale = 2)
    private BigDecimal deduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PayrollStatus {
        PENDING, COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getNetSalary() {
        BigDecimal net = salaryPerMonth;
        if (deduction != null) {
            net = net.subtract(deduction);
        }
        return net;
    }
}