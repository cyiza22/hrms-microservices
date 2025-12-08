package api.example.payrollservice.service;

import api.example.payrollservice.dto.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceClient {

    private final RestTemplate restTemplate;
    private static final String EMPLOYEE_SERVICE_URL = "http://employee-service/api/employees";

    public EmployeeDTO getEmployeeById(Long employeeId) {
        try {
            String url = EMPLOYEE_SERVICE_URL + "/" + employeeId;
            return restTemplate.getForObject(url, EmployeeDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch employee {}: {}", employeeId, e.getMessage());
            return null;
        }
    }
}

