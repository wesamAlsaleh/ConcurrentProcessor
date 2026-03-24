package com.avocadogroup.concurrentprocessor.employee;

import com.avocadogroup.concurrentprocessor.csv.dtos.CsvRowDto;
import com.avocadogroup.concurrentprocessor.employee.dtos.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper interface that converts CsvRowDto objects into EmployeeCsvDto objects.
 * MapStruct generates the implementation class at compile time automatically.
 * The componentModel = "spring" tells MapStruct to make the generated class a Spring bean.
 */
@Mapper(componentModel = "spring") // Register as a Spring-managed bean so it can be injected
public interface EmployeeMapper {

    /**
     * Maps a single CsvRowDto to an EmployeeCsvDto.
     * - joinedDate: converted from String "yyyy-MM-dd" to LocalDate using dateFormat
     * - projectCompletionPercentage: converted from 0.0–1.0 scale to 0–100 scale
     */
    @Mapping(target = "joinedDate", source = "joinedDate", dateFormat = "yyyy-MM-dd") // Convert date string to LocalDate
    @Mapping(target = "projectCompletionPercentage", expression = "java(csvRowDto.getProjectCompletionPercentage() * 100)") // Scale 0.0–1.0 to 0–100
    EmployeeDto toEmployeeDto(CsvRowDto csvRowDto); // Single row conversion

    /**
     * Maps a list of CsvRowDto objects to a list of EmployeeDto objects.
     * MapStruct automatically delegates to the single-item method above for each element.
     */
    List<EmployeeDto> toEmployeeDtoList(List<CsvRowDto> csvRowDtos); // Batch list conversion
}
