package vn.sun.membermanagementsystem.services.impls;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.sun.membermanagementsystem.annotation.LogActivity;
import vn.sun.membermanagementsystem.entities.User;

import vn.sun.membermanagementsystem.dto.request.CreateProjectRequest;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.dto.response.ProjectDetailDTO;
import vn.sun.membermanagementsystem.dto.response.ProjectLeadershipHistoryDTO;
import vn.sun.membermanagementsystem.dto.response.ProjectMemberDTO;
import vn.sun.membermanagementsystem.entities.*;
import vn.sun.membermanagementsystem.enums.MembershipStatus; // Import Enum
import vn.sun.membermanagementsystem.mapper.ProjectMapper;
import vn.sun.membermanagementsystem.repositories.*;
import vn.sun.membermanagementsystem.services.ProjectService;
import vn.sun.membermanagementsystem.services.TeamService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectMapper projectMapper;
    private final TeamService teamService;
    private final ProjectLeadershipHistoryRepository leadershipRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepo;

    private final TeamMemberRepository teamMemberRepo;

    @Override
    public Page<ProjectDTO> getAllProjects(Long teamId, Pageable pageable) {
        Page<Project> projectsPage;

        if (teamId != null) {
            Team team = teamService.getRequiredTeam(teamId);
            projectsPage = projectRepo.findByTeam(team, pageable);
        } else {
            projectsPage = projectRepo.findAll(pageable);
        }

        return projectsPage.map(projectMapper::toDTO);
    }

    @Override
    public ProjectDetailDTO getProjectDetail(Long id) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        ProjectDTO basicDto = projectMapper.toDTO(project);
        ProjectDetailDTO detailDTO = new ProjectDetailDTO();

        detailDTO.setId(basicDto.getId());
        detailDTO.setName(basicDto.getName());
        detailDTO.setAbbreviation(basicDto.getAbbreviation());
        detailDTO.setStartDate(basicDto.getStartDate());
        detailDTO.setEndDate(basicDto.getEndDate());
        detailDTO.setStatus(basicDto.getStatus());
        detailDTO.setTeamId(basicDto.getTeamId());
        detailDTO.setTeamName(basicDto.getTeamName());
        detailDTO.setCreatedAt(basicDto.getCreatedAt());
        detailDTO.setUpdatedAt(basicDto.getUpdatedAt());

        List<ProjectMemberDTO> memberDTOs = project.getProjectMembers().stream()
                .sorted(Comparator.comparing(ProjectMember::getJoinedAt).reversed())
                .map(pm -> ProjectMemberDTO.builder()
                        .id(pm.getId())
                        .userId(pm.getUser().getId())
                        .userName(pm.getUser().getName())
                        .userEmail(pm.getUser().getEmail())
                        .status(pm.getStatus().name())
                        .joinedAt(pm.getJoinedAt())
                        .leftAt(pm.getLeftAt())
                        .build())
                .collect(Collectors.toList());
        detailDTO.setMembers(memberDTOs);

        List<ProjectLeadershipHistoryDTO> historyDTOs = project.getLeadershipHistory().stream()
                .sorted(Comparator.comparing(ProjectLeadershipHistory::getStartedAt).reversed())
                .map(hist -> ProjectLeadershipHistoryDTO.builder()
                        .id(hist.getId())
                        .leaderName(hist.getLeader().getName())
                        .leaderEmail(hist.getLeader().getEmail())
                        .startedAt(hist.getStartedAt())
                        .endedAt(hist.getEndedAt())
                        .isCurrent(hist.getEndedAt() == null)
                        .build())
                .collect(Collectors.toList());
        detailDTO.setLeadershipHistory(historyDTOs);

        return detailDTO;
    }

    @Override
    @Transactional
    @LogActivity(
            action = "CREATE_PROJECT",
            entityType = "PROJECT",
            description = "Create new project"
    )
    public ProjectDTO createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setAbbreviation(request.getAbbreviation());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(Project.ProjectStatus.PLANNING);

        if (request.getTeamId() != null) {
            Team team = teamService.getRequiredTeam(request.getTeamId());
            project.setTeam(team);

            project = projectRepo.save(project);

            if (request.getLeaderId() != null) {
                User leader = userRepo.findById(request.getLeaderId())
                        .orElseThrow(() -> new EntityNotFoundException("Leader not found"));

                boolean isLeaderInTeam = teamMemberRepo.existsByUserAndTeamAndStatus(leader, team, MembershipStatus.ACTIVE);
                if (!isLeaderInTeam) {
                    throw new IllegalArgumentException("Selected Leader is not an active member of the chosen team");
                }

                ProjectLeadershipHistory history = new ProjectLeadershipHistory();
                history.setProject(project);
                history.setLeader(leader);
                history.setStartedAt(java.time.LocalDateTime.now());
                leadershipRepo.save(history);

                saveProjectMember(project, leader);
            }

            List<Long> memberIds = request.getMemberIds();
            if (memberIds != null && !memberIds.isEmpty()) {
                for (Long userId : memberIds) {
                    if (request.getLeaderId() != null && userId.equals(request.getLeaderId())) {
                        continue;
                    }

                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found id: " + userId));

                    boolean isUserInTeam = teamMemberRepo.existsByUserAndTeamAndStatus(user, team, MembershipStatus.ACTIVE);
                    if (!isUserInTeam) {
                        throw new IllegalArgumentException("User " + user.getName() + " is not active in this team");
                    }

                    saveProjectMember(project, user);
                }
            }
        } else {
            project = projectRepo.save(project);
        }

        return projectMapper.toDTO(project);
    }

    private void saveProjectMember(Project project, User user) {
        boolean exists = projectMemberRepo.existsByProjectAndUserAndStatus(project, user, ProjectMember.MemberStatus.ACTIVE);
        if (!exists) {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(project);
            projectMember.setUser(user);
            projectMember.setJoinedAt(java.time.LocalDateTime.now());
            projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
            projectMemberRepo.save(projectMember);
        }
    }
}