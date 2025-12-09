package api.example.leaveservice.service;

import api.example.leaveservice.entity.Leave;
import api.example.leaveservice.dto.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for sending email notifications
 * In a real application, this would integrate with an email service like SendGrid
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final RestTemplate restTemplate;
    private static final String EMPLOYEE_SERVICE_URL = "http://employee-service/api/employees";




    public void sendLeaveApprovalEmail(Leave leave) {
        EmployeeDTO employee = getEmployee(leave.getEmployeeId());
        if (employee != null) {
            log.info("Sending approval email to: {} for leave ID: {}",
                    employee.getEmail(), leave.getId());

        }
    }

    public void sendLeaveRejectionEmail(Leave leave) {
        EmployeeDTO employee = getEmployee(leave.getEmployeeId());
        if (employee != null) {
            log.info("Sending rejection email to: {} for leave ID: {}",
                    employee.getEmail(), leave.getId());

            //emailClient.send(employee.getEmail(), "Leave Rejected", body);
        }
    }

    public void sendLeaveApplicationEmail(Leave leave) {
        EmployeeDTO employee = getEmployee(leave.getEmployeeId());
        if (employee != null) {
            log.info("Sending application confirmation email to: {} for leave ID: {}",
                    employee.getEmail(), leave.getId());


            //emailClient.send(employee.getEmail(), "Leave Applied", body);
        }
    }

    private EmployeeDTO getEmployee(Long employeeId) {
        try {
            return restTemplate.getForObject(
                    EMPLOYEE_SERVICE_URL + "/" + employeeId,
                    EmployeeDTO.class
            );
        } catch (Exception e) {
            log.error("Failed to fetch employee {}: {}", employeeId, e.getMessage());
            return null;
        }
    }
}