package com.avocadogroup.concurrentprocessor.csv.dtos;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) representing a single raw row from the CSV file.
 * Each field is mapped to a column in the CSV by its position (0-indexed).
 * OpenCSV uses the @CsvBindByPosition annotation to automatically parse each column.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor //
public class CsvRowDto {
    @CsvBindByPosition(position = 0) // Mapped to CSV column 0
    private int id;

    @CsvBindByPosition(position = 1) // Mapped to CSV column 1
    private String name;

    @CsvBindByPosition(position = 2) // Mapped to CSV column 2
    private double salary;

    @CsvBindByPosition(position = 3) // Mapped to CSV column 3
    private String joinedDate;

    @CsvBindByPosition(position = 4) // Mapped to CSV column 4
    private String role;

    @CsvBindByPosition(position = 5) // Mapped to CSV column 5
    private double projectCompletionPercentage;
}
