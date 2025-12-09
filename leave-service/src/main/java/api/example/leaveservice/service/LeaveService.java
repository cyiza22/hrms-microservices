package api.example.leaveservice.service;

import api.example.leaveservice.dto.LeaveDTO;
import api.example.leaveservice.dto.EmployeeDTO;
import api.example.leaveservice.entity.Leave;
import api.example.leaveservice.repository.LeaveRepository;
import api.example.leaveservice.observer.LeaveObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final RestTemplate restTemplate;
    private static final String EMPLOYEE_SERVICE_URL = "http://employee-service/api/employees";
    private final List<LeaveObserver> observers;

    private void notifyObservers(Leave leave) {
        log.info("Notifying {} observers about leave status change", observers.size());
        observers.forEach(observer -> {
            try {
                log.debug("Notifying observer: {}", observer.getObserverName());
                observer.onLeaveStatusChanged(leave);
            } catch (Exception e) {
                log.error("Observer {} failed: {}",
                        observer.getObserverName(), e.getMessage());
            }
        });
    }

    @Transactional
    public LeaveDTO applyLeave(LeaveDTO dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        EmployeeDTO employee = getEmployee(dto.getEmployeeId());
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        Leave leave = Leave.builder()
                .employeeId(dto.getEmployeeId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(Leave.LeaveStatus.PENDING)
                .build();

        Leave saved = leaveRepository.save(leave);
        log.info("Leave applied by employee: {}", dto.getEmployeeId());

        // NOTIFY OBSERVERS
        notifyObservers(saved);

        return mapToDTO(saved, employee);
    }

    @Transactional
    public LeaveDTO approveLeave(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(Leave.LeaveStatus.APPROVED);
        Leave updated = leaveRepository.save(leave);
        log.info("Leave approved: {}", id);

        // NOTIFY OBSERVERS
        notifyObservers(updated);

        return mapToDTO(updated);
    }

    @Transactional
    public LeaveDTO rejectLeave(Long id, String rejectionReason) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(Leave.LeaveStatus.REJECTED);
        leave.setReason(leave.getReason() + " (Rejected: " + rejectionReason + ")");
        Leave updated = leaveRepository.save(leave);
        log.info("Leave rejected: {}", id);

        // NOTIFY OBSERVERS
        notifyObservers(updated);

        return mapToDTO(updated);
    }



    public List<LeaveDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveDTO> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveDTO> getPendingLeaves() {
        return leaveRepository.findByStatus(Leave.LeaveStatus.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }



    private EmployeeDTO getEmployee(Long employeeId) {
        try {
            return restTemplate.getForObject(EMPLOYEE_SERVICE_URL + "/" + employeeId, EmployeeDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch employee: {}", e.getMessage());
            return null;
        }
    }

    private LeaveDTO mapToDTO(Leave leave) {
        EmployeeDTO employee = getEmployee(leave.getEmployeeId());
        return mapToDTO(leave, employee);
    }

    private LeaveDTO mapToDTO(Leave leave, EmployeeDTO employee) {
        LeaveDTO dto = new LeaveDTO();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus().name());

        if (employee != null) {
            dto.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            dto.setEmployeeEmail(employee.getEmail());
        }

        return dto;
    }
}
