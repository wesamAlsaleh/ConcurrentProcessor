package com.avocadogroup.concurrentprocessor.concurrency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spring configuration class that provides a thread pool (ExecutorService) bean.
 * The thread pool is used to process employee salary calculations concurrently.
 * A fixed thread pool of 5 threads is created, meaning up to 5 tasks can run in parallel.
 */
@Configuration // Tells Spring this class contains bean definitions
public class ExecutorConfig {
    /**
     * Creates and registers a fixed-size thread pool as a Spring bean.
     * Fixed thread pool means exactly 5 threads are created and reused.
     * If all 5 threads are busy, new tasks wait in a queue until a thread is available.
     *
     * @return an ExecutorService with a fixed pool of 5 threads
     */
    @Bean // Registers the returned object as a Spring-managed bean
    public ExecutorService employeeExecutorService() {
        // Create a thread pool with exactly 5 worker threads
        return Executors.newFixedThreadPool(5); // Fixed-size thread pool
    }
}
