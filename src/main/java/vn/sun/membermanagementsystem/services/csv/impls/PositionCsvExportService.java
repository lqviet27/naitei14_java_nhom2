package vn.sun.membermanagementsystem.services.csv.impls;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.entities.Position;
import vn.sun.membermanagementsystem.repositories.PositionRepository;
import vn.sun.membermanagementsystem.services.csv.CsvExportService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionCsvExportService implements CsvExportService<Position> {

    private final PositionRepository positionRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public void exportToCsv(OutputStream outputStream) throws IOException {
        log.info("Starting export of positions to CSV");

        List<Position> positions = positionRepository.findAllNotDeleted();

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            // Write BOM for Excel UTF-8 support
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            writer.writeNext(getExportHeaders());

            for (Position position : positions) {
                String[] row = convertPositionToRow(position);
                writer.writeNext(row);
            }

            log.info("Successfully exported {} positions to CSV", positions.size());
        }
    }

    private String[] convertPositionToRow(Position position) {
        List<String> row = new ArrayList<>();

        row.add(position.getId() != null ? position.getId().toString() : "");
        row.add(position.getName() != null ? position.getName() : "");
        row.add(position.getAbbreviation() != null ? position.getAbbreviation() : "");
        row.add(position.getCreatedAt() != null ? position.getCreatedAt().format(DATETIME_FORMATTER) : "");
        row.add(position.getUpdatedAt() != null ? position.getUpdatedAt().format(DATETIME_FORMATTER) : "");

        return row.toArray(new String[0]);
    }

    @Override
    public String[] getExportHeaders() {
        return new String[]{
                "ID",
                "Name",
                "Abbreviation",
                "Created At",
                "Updated At"
        };
    }
}
