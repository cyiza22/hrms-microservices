package api.example.projectservice.service;

import api.example.projectservice.dto.ProjectDTO;
import api.example.projectservice.dto.EmployeeDTO;
import api.example.projectservice.entity.Project;
import api.example.projectservice.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final RestTemplate restTemplate;
    private static final String EMPLOYEE_SERVICE_URL = "http://employee-service/api/employees";

    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        EmployeeDTO employee = getEmployee(dto.getAssignedToEmployeeId());
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        Project project = Project.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(Project.ProjectStatus.valueOf(dto.getStatus()))
                .assignedToEmployeeId(dto.getAssignedToEmployeeId())
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project created: {}", saved.getName());
        return mapToDTO(saved, employee);
    }

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToDTO(project);
    }

    public List<ProjectDTO> getProjectsByEmployeeId(Long employeeId) {
        return projectRepository.findByAssignedToEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setName(dto.getName());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(Project.ProjectStatus.valueOf(dto.getStatus()));
        project.setAssignedToEmployeeId(dto.getAssignedToEmployeeId());

        Project updated = projectRepository.save(project);
        log.info("Project updated: {}", id);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found");
        }
        projectRepository.deleteById(id);
        log.info("Project deleted: {}", id);
    }

    private EmployeeDTO getEmployee(Long employeeId) {
        try {
            return restTemplate.getForObject(EMPLOYEE_SERVICE_URL + "/" + employeeId, EmployeeDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch employee: {}", e.getMessage());
            return null;
        }
    }

    private ProjectDTO mapToDTO(Project project) {
        EmployeeDTO employee = getEmployee(project.getAssignedToEmployeeId());
        return mapToDTO(project, employee);
    }

    private ProjectDTO mapToDTO(Project project, EmployeeDTO employee) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus().name());
        dto.setAssignedToEmployeeId(project.getAssignedToEmployeeId());

        if (employee != null) {
            dto.setAssignedEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            dto.setAssignedEmployeeEmail(employee.getEmail());
        }

        return dto;
    }
}
