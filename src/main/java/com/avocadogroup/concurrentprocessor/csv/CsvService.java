package com.avocadogroup.concurrentprocessor.csv;

import com.avocadogroup.concurrentprocessor.csv.dtos.CsvRowDto;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

/**
 * Service responsible for reading and parsing the CSV file.
 * Uses OpenCSV library to convert each CSV row into a CsvRowDto object.
 */
@Service
public class CsvService {
    // The file path to the CSV file, injected from application.yaml property "csv.file.path"
    @Value("${csv.file.path}")
    private String csvFilePath;

    /**
     * Reads the CSV file and converts each row into a CsvRowDto object.
     *
     * @return a list of CsvRowDto objects representing all rows in the CSV file
     */
    public List<CsvRowDto> readCsvFile() {
        // Create a FileReader to read the CSV file from the configured path
        try (
                Reader reader = new FileReader(csvFilePath)
        ) {
            // Use OpenCSV's CsvToBeanBuilder to parse the CSV file
            return new CsvToBeanBuilder<CsvRowDto>(reader)
                    .withType(CsvRowDto.class) // Tell OpenCSV which DTO class to map rows to
                    .build() // Build the CsvToBean parser
                    .parse(); // Parse all rows and return as a list of CsvRowDto
        } catch (Exception e) {
            // Wrap any exception in a RuntimeException with a descriptive message
            throw new RuntimeException("Failed to read CSV file at path: " + csvFilePath + " — " + e.getMessage(), e);
        }
    }
}
