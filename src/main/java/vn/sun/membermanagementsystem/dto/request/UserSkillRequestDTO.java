package vn.sun.membermanagementsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.entities.UserSkill;

import java.math.BigDecimal;

/**
 * DTO for skill entry in user create/update forms.
 * Reusable across UserCreateDTO and UserUpdateDTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillRequestDTO {
    private Long skillId;
    private UserSkill.Level level;
    private BigDecimal usedYearNumber;
}
