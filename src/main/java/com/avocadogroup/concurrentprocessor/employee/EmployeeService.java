package com.avocadogroup.concurrentprocessor.employee;

import com.avocadogroup.concurrentprocessor.concurrency.LockService;
import com.avocadogroup.concurrentprocessor.concurrency.SemaphoreService;
import com.avocadogroup.concurrentprocessor.csv.CsvService;
import com.avocadogroup.concurrentprocessor.csv.dtos.CsvRowDto;
import com.avocadogroup.concurrentprocessor.employee.dtos.EmployeeDto;
import com.avocadogroup.concurrentprocessor.employee.dtos.ProcessedEmployeeDto;

import java.util.logging.Logger;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core service containing ALL the business logic for processing employee salaries.
 * <p>
 * This service:
 * 1. Reads employee data from a CSV file via CsvService
 * 2. Maps raw CSV rows to typed DTOs via MapStruct (EmployeeMapper)
 * 3. Processes each employee's salary concurrently using a thread pool
 * 4. Applies business rules (project completion, years of service, role-based increases)
 * 5. Uses ReentrantLock, Semaphore, and AtomicInteger for thread safety
 */
@Service
@AllArgsConstructor
public class EmployeeService {
    private final CsvService csvService;
    private final EmployeeMapper employeeMapper;
    private final ExecutorService executorService;
    private final LockService lockService;
    private final SemaphoreService semaphoreService;
    private final AtomicInteger processedCount = new AtomicInteger(0); // Atomic counter to track how many employees have been processed (thread-safe)

    // Logger for printing informational messages to the console
    private static final Logger logger = Logger.getLogger(EmployeeService.class.getName());

    /**
     * Applies all salary increase business rules to a single employee.
     * <p>
     * Rules:
     * 1. Project Completion: if projectCompletionPercentage < 60 → NO increase at all
     * 2. Years of Service: if >= 1 year → +2% per year of service
     * 3. Role-Based: Director → +5%, Manager → +2%, Employee → +1%
     * 4. Final salary = originalSalary + (originalSalary × totalIncreasePercentage / 100)
     *
     * @param employee the employee DTO to process
     * @return a ProcessedEmployeeDto containing original salary, new salary, and breakdown
     */
    private ProcessedEmployeeDto calculateSalary(EmployeeDto employee) {
        // Get the original salary before any modifications
        double originalSalary = employee.getSalary();

        // Initialize total increase percentage to 0
        double totalIncreasePercentage = 0.0;

        // If the employee's project completion is below 60%, they are not eligible for any salary increase
        boolean eligible = !(employee.getProjectCompletionPercentage() < 60);

        // Calculate how many full years the employee has worked at the company
        long yearsOfService = ChronoUnit.YEARS.between(employee.getJoinedDate(), LocalDate.now());

        // Only apply increases if the employee is eligible (project completion >= 60%)
        if (eligible) {
            // If the employee has completed at least 1 year, add 2% per year
            if (yearsOfService >= 1) {
                totalIncreasePercentage += yearsOfService * 2.0; // 2% multiplied by number of years
            }

            // Add a bonus percentage based on the employee's role
            switch (employee.getRole()) { // Check the role string
                case "Director": // Directors get the highest role bonus
                    totalIncreasePercentage += 5.0; // Add 5% for Director role
                    break;
                case "Manager": // Managers get a moderate role bonus
                    totalIncreasePercentage += 2.0; // Add 2% for Manager role
                    break;
                case "Employee": // Regular employees get the smallest role bonus
                    totalIncreasePercentage += 1.0; // Add 1% for Employee role
                    break;
                default: // If the role doesn't match any known role
                    logger.warning(String.format("Unknown role '%s' for employee: %s", employee.getRole(), employee.getName()));
                    break;
            }
        }

        // Calculate Final Salary, newSalary = originalSalary + (originalSalary × totalIncreasePercentage / 100)
        double increaseAmount = originalSalary * totalIncreasePercentage / 100; // Calculate the amount of the increase
        double newSalary = originalSalary + increaseAmount; // Add the increase to the original salary

        // Build and return the processed employee DTO using the builder pattern
        return ProcessedEmployeeDto.builder()
                .id(employee.getId()) // Set the employee ID
                .name(employee.getName()) // Set the employee name
                .originalSalary(originalSalary) // Set the salary before increase
                .newSalary(Math.round(newSalary * 100.0) / 100.0) // Round to 2 decimal places
                .totalIncreasePercentage(totalIncreasePercentage) // Set the total % increase applied
                .role(employee.getRole()) // Set the employee role
                .joinedDate(employee.getJoinedDate()) // Set the join date
                .yearsOfService((int) yearsOfService) // Set years of service
                .projectCompletionPercentage(employee.getProjectCompletionPercentage()) // Set project completion %
                .eligible(eligible) // Set whether the employee was eligible for increases
                .processedByThread(Thread.currentThread().getName()) // Record which thread did the processing
                .build(); // Build the final ProcessedEmployeeDto object
    }

    /**
     * Main method that orchestrates the entire employee processing pipeline.
     * Steps:
     * 1. Read CSV → List of CsvRowDto
     * 2. Map to List of EmployeeCsvDto (via MapStruct)
     * 3. Process each employee concurrently in the thread pool
     * 4. Return List of ProcessedEmployeeDto with updated salaries
     *
     * @return list of processed employees with original and new salary information
     * @throws InterruptedException if the main thread is interrupted while waiting
     */
    public List<ProcessedEmployeeDto> processEmployees() throws InterruptedException {
        // Reset the atomic counter to 0 for each new processing run
        processedCount.set(0);

        // Read all rows from the CSV file
        logger.info("Reading CSV file..."); // Log the start of CSV reading
        List<CsvRowDto> csvRows = csvService.readCsvFile(); // Read the CSV file and return the data as list
        logger.info(String.format("Read %d rows from CSV file.", csvRows.size())); // Log how many rows were read

        // Convert raw CSV rows to typed employee DTOs using MapStruct
        logger.info("Mapping CSV rows to Employee DTOs..."); // Log mapping start
        List<EmployeeDto> employees = employeeMapper.toEmployeeDtoList(csvRows); // Convert the list
        logger.info(String.format("Mapped %d employees.", employees.size())); // Log mapping result

        // Create a thread-safe list to collect results from multiple threads
        List<ProcessedEmployeeDto> results = new ArrayList<>(); // Shared result list (protected by lock)

        // Iterate over all employees and submit each employee as a separate task to the thread pool
        for (EmployeeDto employee : employees) {
            executorService.submit(() -> { // Submit a new task (Runnable) to the thread pool to be executed
                try {
                    // Acquire a semaphore permit — blocks if all permits are taken
                    // This limits concurrent processing to the number of semaphore permits (MAX_CONNECTIONS = 3)
                    semaphoreService.acquire();

                    try {
                        // Log which thread is processing which employee
                        logger.info(String.format("[%s] Processing employee: %s", Thread.currentThread().getName(), employee.getName()));

                        // Calculate the new salary based on business rules
                        ProcessedEmployeeDto processed = calculateSalary(employee);

                        // Acquire the ReentrantLock before modifying the shared results list
                        // This ensures only one thread can add to the list at a time
                        lockService.lock();

                        try {
                            results.add(processed); // Safely add the result to the shared list
                        } finally {
                            lockService.unlock(); // Release the lock
                        }

                        // Atomically increment the processed count (thread-safe without locking)
                        int count = processedCount.incrementAndGet();

                        logger.info(String.format("[%s] Completed employee: %s (%d/%d)",
                                Thread.currentThread().getName(), employee.getName(), count, employees.size())); // Log the progress

                    } finally {
                        // Release the semaphore permit to prevent leaks
                        semaphoreService.release();
                    }

                } catch (InterruptedException e) {
                    // If the thread is interrupted while waiting for the semaphore
                    Thread.currentThread().interrupt(); // Restore the interrupt flag
                    logger.fine(String.format("Thread interrupted while processing employee: %s", employee.getName()));
                }
            });
        }

        // Log the final summary
        logger.info(String.format("All %d employees processed successfully!", processedCount.get()));

        // Return the complete list of processed employees
        return results;
    }

    /**
     * Returns the current value of the atomic processed counter.
     * This is thread-safe and can be called from any thread.
     *
     * @return the number of employees that have been processed so far
     */
    public int getProcessedCount() {
        return processedCount.get(); // Atomically read the current count
    }
}
