package com.avocadogroup.concurrentprocessor.concurrency;

import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

/**
 * Service that wraps a Semaphore to limit the number of threads that can
 * process employees concurrently.
 * <p>
 * Unlike a Lock (which allows only 1 thread), a Semaphore allows up to N threads
 * to access a resource at the same time. Here, N = 3 permits.
 * <p>
 * This means at most 3 employee salary calculations can happen simultaneously,
 * even though the thread pool has 5 threads (Executor workers). This is useful for controlling
 * resource usage.
 */
@Service
public class SemaphoreService {
    // Declare N threads can process at the same time
    private static final int MAX_CONNECTIONS = 3;

    // Create a Semaphore (Threads Connection pool)
    private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS);

    /**
     * Acquires a permit from the semaphore. If all 3 permits are taken,
     * the calling thread will WAIT until another thread releases a permit.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        semaphore.acquire(); // Block until a permit is available, then take it
    }

    /**
     * Releases a permit back to the semaphore, allowing another waiting thread to proceed.
     * This should ALWAYS be called in a finally block to prevent permit leaks.
     */
    public void release() {
        semaphore.release(); // Return a permit to the semaphore
    }
}
