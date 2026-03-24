package com.avocadogroup.concurrentprocessor.employee;

import com.avocadogroup.concurrentprocessor.employee.dtos.ProcessedEmployeeDto;

import java.util.logging.Logger;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller that exposes the employee processing API endpoint.
 * This controller contains NO business logic — it only delegates to EmployeeService.
 * All salary calculation and concurrency logic lives in EmployeeService.
 */
@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // Logger for printing informational messages to the console
    private static final Logger logger = Logger.getLogger(EmployeeController.class.getName());

    /**
     * POST /api/employees/process
     * <p>
     * Triggers the concurrent processing of all employees from the CSV file.
     * Returns a JSON response containing the total processed count and the list of results.
     *
     * @return ResponseEntity with a map containing "totalProcessed" and "employees" keys
     * @throws InterruptedException if the processing is interrupted
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processEmployees() throws InterruptedException {
        // Log that the processing request was received
        logger.info("Received request to process employees.");

        // Delegate to the service to perform the actual processing
        List<ProcessedEmployeeDto> results = employeeService.processEmployees();

        // Build the response map with summary and detailed results
        Map<String, Object> response = new HashMap<>(); // Create a new HashMap for the response body
        response.put("totalProcessed", employeeService.getProcessedCount()); // Add total count to response
        response.put("employees", results); // Add the list of processed employees to response

        // Log that the processing completed successfully
        logger.info(String.format("Processing complete. Returning %d results.", results.size()));

        // Return HTTP 200 OK with the response body as JSON
        return ResponseEntity.ok(response);
    }
}
