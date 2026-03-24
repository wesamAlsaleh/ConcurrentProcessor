package com.avocadogroup.concurrentprocessor.employee.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing an employee after salary processing has been completed.
 * Contains both the original and new salary, the increase breakdown,
 * eligibility status, and which thread performed the processing.
 */
@Data
@AllArgsConstructor
@Builder // enables the builder pattern (e.g., ProcessedEmployeeDto.builder().name("X").build())
public class ProcessedEmployeeDto {
    private int id;
    private String name;
    private double originalSalary; // The employee's salary BEFORE any increases were applied
    private double newSalary; // The employee's salary AFTER all applicable increases were applied
    private double totalIncreasePercentage;
    private String role;
    private LocalDate joinedDate;
    private int yearsOfService;
    private double projectCompletionPercentage;
    private boolean eligible;
    private String processedByThread;
}
