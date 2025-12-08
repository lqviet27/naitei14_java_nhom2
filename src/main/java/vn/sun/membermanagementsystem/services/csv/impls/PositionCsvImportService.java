package vn.sun.membermanagementsystem.services.csv.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Position;
import vn.sun.membermanagementsystem.repositories.PositionRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionCsvImportService extends AbstractCsvImportService<Position> {

    private final PositionRepository positionRepository;
    private static final int COL_NAME = 0;
    private static final int COL_ABBREVIATION = 1;

    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return validateRowData(data);
    }

    private List<String> validateRowData(String[] data) {
        List<String> errors = new ArrayList<>();

        // Validate name (required)
        String name = getStringValue(data, COL_NAME);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must be less than 255 characters");
        } else if (positionRepository.existsByNameIgnoreCaseAndNotDeleted(name, null)) {
            errors.add("Position name already exists: " + name);
        }

        // Validate abbreviation (required)
        String abbreviation = getStringValue(data, COL_ABBREVIATION);
        if (isBlank(abbreviation)) {
            errors.add("Abbreviation is required");
        } else if (abbreviation.length() > 50) {
            errors.add("Abbreviation must be less than 50 characters");
        } else if (positionRepository.existsByAbbreviationIgnoreCaseAndNotDeleted(abbreviation, null)) {
            errors.add("Abbreviation already exists: " + abbreviation);
        }

        return errors;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsvImportResult<Position> importFromCsv(MultipartFile file) {
        return super.importFromCsv(file);
    }

    @Override
    protected Position processRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
        String name = getStringValue(data, COL_NAME);
        String abbreviation = getStringValue(data, COL_ABBREVIATION);

        // Create new Position entity
        Position position = new Position();
        position.setName(name.trim());
        position.setAbbreviation(abbreviation.trim().toUpperCase());
        position.setCreatedAt(LocalDateTime.now());
        position.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Position savedPosition = positionRepository.save(position);
        log.info("Row {}: Created position '{}' ({}) with ID: {}", 
                rowNumber, savedPosition.getName(), savedPosition.getAbbreviation(), savedPosition.getId());

        return savedPosition;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
        List<String> errors = validateRowData(data);

        for (String error : errors) {
            result.addError(rowNumber, "Validation", error);
        }

        return errors.isEmpty();
    }

    @Override
    public String[] getExpectedHeaders() {
        return new String[]{
                "Name",
                "Abbreviation"
        };
    }

    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", getExpectedHeaders())).append("\n");
        sb.append("Software Engineer,SE\n");
        sb.append("Project Manager,PM\n");
        sb.append("Business Analyst,BA\n");
        sb.append("Quality Assurance,QA\n");
        return sb.toString();
    }
}
