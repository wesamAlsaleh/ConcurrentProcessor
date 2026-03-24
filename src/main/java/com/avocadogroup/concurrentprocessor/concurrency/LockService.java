package com.avocadogroup.concurrentprocessor.concurrency;

import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Service that wraps a ReentrantLock to provide thread-safe access to shared resources.
 * A ReentrantLock ensures that only ONE thread can execute a critical section at a time.
 * "Reentrant" means the same thread can acquire the lock multiple times without deadlocking.
 * <p>
 * In this project, the lock is used to safely add processed employee results to a shared list.
 */
@Service
public class LockService {
    // Create a ReentrantLock instance
    private final ReentrantLock lock = new ReentrantLock(); // the actual lock object

    /**
     * Acquires the lock. If another thread already holds the lock,
     * the current thread will WAIT (block) until the lock is released.
     * IMPORTANT: Every call to lock() MUST have a corresponding unlock() call.
     */
    public void lock() {
        lock.lock(); // Acquire the lock (blocks if already held by another thread)
    }

    /**
     * Releases the lock so that other waiting threads can acquire it.
     * This should ALWAYS be called in a finally block to prevent deadlocks.
     */
    public void unlock() {
        lock.unlock(); // Release the lock
    }
}
