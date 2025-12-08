package api.example.payrollservice.controller;

import api.example.payrollservice.dto.PayrollDTO;
import api.example.payrollservice.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<PayrollDTO> createPayroll(@Valid @RequestBody PayrollDTO dto) {
        return ResponseEntity.ok(payrollService.createPayroll(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('HR', 'MANAGER')")
    public ResponseEntity<List<PayrollDTO>> getAllPayrolls() {
        return ResponseEntity.ok(payrollService.getAllPayrolls());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('HR', 'MANAGER')")
    public ResponseEntity<PayrollDTO> getPayrollById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<PayrollDTO>> getEmployeePayrolls(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getPayrollsByEmployeeId(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<PayrollDTO> updatePayroll(
            @PathVariable Long id,
            @Valid @RequestBody PayrollDTO dto) {
        return ResponseEntity.ok(payrollService.updatePayroll(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> deletePayroll(@PathVariable Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.noContent().build();
    }
}