package vn.sun.membermanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.entities.UserSkill;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for displaying user profile details (view page).
 * Contains all user information for read-only display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDetailDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDate birthday;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String activeTeam;
    private List<ProjectInfo> activeProjects;
    private PositionInfo currentPosition;
    private List<SkillInfo> skills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Long id;
        private String name;
        private String abbreviation;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionInfo {
        private Long id;
        private String name;
        private String abbreviation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long skillId;
        private String skillName;
        private UserSkill.Level level;
        private BigDecimal usedYearNumber;
    }
}
