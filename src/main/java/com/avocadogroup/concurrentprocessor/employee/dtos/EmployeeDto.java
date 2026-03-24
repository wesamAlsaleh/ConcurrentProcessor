package com.avocadogroup.concurrentprocessor.employee.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO representing an employee after CSV data has been parsed and mapped.
 * The joinedDate is a proper LocalDate (not a String like in CsvRowDto).
 * The projectCompletionPercentage is scaled to 0–100 (not 0.0–1.0 like in the CSV).
 */
@Data
@AllArgsConstructor
public class EmployeeDto {
    private int id;
    private String name;
    private double salary;
    private LocalDate joinedDate;
    private String role;
    private double projectCompletionPercentage;
}
