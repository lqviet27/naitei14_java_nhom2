package vn.sun.membermanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    @NotBlank(message = "Abbreviation is required")
    private String abbreviation;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Team ID is required")
    private Long teamId;

    private Long leaderId;

    private List<Long> memberIds;
}
