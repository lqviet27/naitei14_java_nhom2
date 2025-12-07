package vn.sun.membermanagementsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.entities.UserSkill;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private Long id;

    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    private String password;

    private LocalDate birthday;

    private UserRole role;
    
    private UserStatus status;
    
    private Long positionId;
    
    private String positionName;

    @Builder.Default
    private List<SkillEntry> skills = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntry {
        private Long skillId;
        private String skillName;
        private UserSkill.Level level;
        private BigDecimal usedYearNumber;
    }
}
