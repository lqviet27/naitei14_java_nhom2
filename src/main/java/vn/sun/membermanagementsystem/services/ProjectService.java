package vn.sun.membermanagementsystem.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.sun.membermanagementsystem.dto.request.CreateProjectRequest;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.dto.response.ProjectDetailDTO;


public interface ProjectService {
    Page<ProjectDTO> getAllProjects(Long teamId, Pageable pageable);
    ProjectDetailDTO getProjectDetail(Long id);
    ProjectDTO createProject(CreateProjectRequest request);
}