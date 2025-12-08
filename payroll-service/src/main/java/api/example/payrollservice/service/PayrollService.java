package api.example.payrollservice.service;

import api.example.payrollservice.dto.EmployeeDTO;
import api.example.payrollservice.dto.PayrollDTO;
import api.example.payrollservice.entity.Payroll;
import api.example.payrollservice.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeServiceClient employeeServiceClient;

    @Transactional
    public PayrollDTO createPayroll(PayrollDTO dto) {
        // Verify employee exists
        EmployeeDTO employee = employeeServiceClient.getEmployeeById(dto.getEmployeeId());
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        Payroll payroll = Payroll.builder()
                .employeeId(dto.getEmployeeId())
                .ctc(dto.getCtc())
                .salaryPerMonth(dto.getSalaryPerMonth())
                .deduction(dto.getDeduction())
                .status(Payroll.PayrollStatus.valueOf(dto.getStatus()))
                .build();

        Payroll saved = payrollRepository.save(payroll);
        log.info("Created payroll for employee: {}", dto.getEmployeeId());
        return mapToDTO(saved, employee);
    }

    public List<PayrollDTO> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PayrollDTO getPayrollById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        return mapToDTO(payroll);
    }

    public List<PayrollDTO> getPayrollsByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollDTO updatePayroll(Long id, PayrollDTO dto) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setCtc(dto.getCtc());
        payroll.setSalaryPerMonth(dto.getSalaryPerMonth());
        payroll.setDeduction(dto.getDeduction());
        payroll.setStatus(Payroll.PayrollStatus.valueOf(dto.getStatus()));

        Payroll updated = payrollRepository.save(payroll);
        log.info("Updated payroll: {}", id);
        return mapToDTO(updated);
    }

    @Transactional
    public void deletePayroll(Long id) {
        if (!payrollRepository.existsById(id)) {
            throw new RuntimeException("Payroll not found");
        }
        payrollRepository.deleteById(id);
        log.info("Deleted payroll: {}", id);
    }

    private PayrollDTO mapToDTO(Payroll payroll) {
        EmployeeDTO employee = employeeServiceClient.getEmployeeById(payroll.getEmployeeId());
        return mapToDTO(payroll, employee);
    }

    private PayrollDTO mapToDTO(Payroll payroll, EmployeeDTO employee) {
        PayrollDTO dto = new PayrollDTO();
        dto.setId(payroll.getId());
        dto.setEmployeeId(payroll.getEmployeeId());
        dto.setCtc(payroll.getCtc());
        dto.setSalaryPerMonth(payroll.getSalaryPerMonth());
        dto.setDeduction(payroll.getDeduction());
        dto.setStatus(payroll.getStatus().name());
        dto.setNetSalary(payroll.getNetSalary());
        dto.setCreatedAt(payroll.getCreatedAt());
        dto.setUpdatedAt(payroll.getUpdatedAt());

        if (employee != null) {
            dto.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            dto.setEmployeeEmail(employee.getEmail());
        }

        return dto;
    }
}