package api.example.projectservice.repository;

import api.example.projectservice.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAssignedToEmployeeId(Long employeeId);
    List<Project> findByStatus(Project.ProjectStatus status);
}