package vn.sun.membermanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.sun.membermanagementsystem.dto.request.CreateProjectRequest;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.dto.response.ProjectDetailDTO;
import vn.sun.membermanagementsystem.dto.response.UserSelectionDTO;
import vn.sun.membermanagementsystem.services.ProjectService;
import vn.sun.membermanagementsystem.services.TeamService;

import java.util.List;

@Controller
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectService projectService;
    private final TeamService teamService;

    private static final List<Integer> PAGE_SIZES = List.of(10, 25, 50, 100);

    @GetMapping
    public String listProjects(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long teamId
    ) {
        if (!PAGE_SIZES.contains(size)) {
            size = 10;
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<ProjectDTO> projectsPage = projectService.getAllProjects(teamId, pageable);

        model.addAttribute("projects", projectsPage);
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("selectedTeamId", teamId);
        model.addAttribute("currentPageSize", size);
        model.addAttribute("pageSizeOptions", PAGE_SIZES);

        return "admin/projects/index";
    }

    @GetMapping("/{id}")
    public String viewProjectDetail(@PathVariable Long id, Model model) {
        ProjectDetailDTO project = projectService.getProjectDetail(id);
        model.addAttribute("project", project);
        return "admin/projects/detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("projectRequest")) {
            model.addAttribute("projectRequest", new CreateProjectRequest());
        }
        model.addAttribute("teams", teamService.getAllTeams());
        return "admin/projects/create";
    }

    @PostMapping("/create")
    public String createProject(
            @Valid @ModelAttribute("projectRequest") CreateProjectRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.getAllTeams());
            if (request.getTeamId() != null) {
                model.addAttribute("preloadedUsers", teamService.getActiveUsersByTeam(request.getTeamId()));
            }
            return "admin/projects/create";
        }

        try {
            ProjectDTO newProject = projectService.createProject(request);
            redirectAttributes.addFlashAttribute("successMessage", "Project '" + newProject.getName() + "' created successfully!");
            return "redirect:/admin/projects/" + newProject.getId();
        } catch (IllegalArgumentException e) {
            result.rejectValue("teamId", "error.projectRequest", e.getMessage());
            model.addAttribute("teams", teamService.getAllTeams());
            if (request.getTeamId() != null) {
                model.addAttribute("preloadedUsers", teamService.getActiveUsersByTeam(request.getTeamId()));
            }
            return "admin/projects/create";
        }
    }

    @GetMapping("/api/teams/{teamId}/users")
    @ResponseBody
    public ResponseEntity<List<UserSelectionDTO>> getUsersByTeamApi(@PathVariable Long teamId) {
        List<UserSelectionDTO> users = teamService.getActiveUsersByTeam(teamId);
        return ResponseEntity.ok(users);
    }
}
