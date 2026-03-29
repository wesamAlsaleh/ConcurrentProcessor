# ConcurrentProcessor — Employee Salary Processing with Multithreading

A Spring Boot application that reads employee data from a CSV file and processes salary increments **concurrently** using Java's built-in concurrency utilities.

---

## 🔄 Concurrency Design

This project demonstrates **four** key Java concurrency mechanisms:

### 1. ExecutorService (Thread Pool)
- Configured in `ExecutorConfig.java` as a **fixed thread pool with 5 threads**.
- Each employee is submitted as a separate task to the pool via `executorService.submit()`.
- The pool reuses threads — if all 5 are busy, new tasks wait in an internal queue.

### 2. Semaphore (Concurrency Limiter)
- Configured in `SemaphoreService.java` with **3 permits**.
- Even though the thread pool has 5 threads, only 3 can process employees **at the same time**.
- This simulates real-world scenarios where you want to limit access to a shared resource (e.g., database connections, API rate limits).
- Each thread calls `semaphore.acquire()` before processing and `semaphore.release()` after.

### 3. ReentrantLock (Mutual Exclusion)
- Configured in `LockService.java`.
- Used to protect the **shared results list** — only one thread can add a result at a time.
- The lock is acquired with `lock()` and released with `unlock()` in a `finally` block to prevent deadlocks.

---

## 💰 Salary Increase Business Rules

All rules are applied in `EmployeeService.calculateSalary()`:

### Rule 1 — Project Completion Gate
| Condition                            | Result                          |
|--------------------------------------|---------------------------------|
| `projectCompletionPercentage < 60%`  | ❌ **No salary increase at all** |
| `projectCompletionPercentage >= 60%` | ✅ Proceed to Rules 2 & 3        |

### Rule 2 — Years of Service
| Condition        | Increase                    |
|------------------|-----------------------------|
| Less than 1 year | 0%                          |
| 1+ years         | **+2% per year** of service |

*Example: An employee who joined 5 years ago gets +10%*

### Rule 3 — Role-Based Bonus
| Role     | Increase |
|----------|----------|
| Director | **+5%**  |
| Manager  | **+2%**  |
| Employee | **+1%**  |

### Rule 4 — Final Calculation
```
finalSalary = originalSalary + (originalSalary × totalIncreasePercentage / 100)
```

*Where `totalIncreasePercentage = yearsOfServiceIncrease + roleIncrease` (if eligible)*

### Example
| Field              | Value                     |
|--------------------|---------------------------|
| Name               | Alice                     |
| Salary             | $52,000                   |
| Joined             | 2019-05-12 (≈6 years ago) |
| Role               | Employee                  |
| Project Completion | 80%                       |

- ✅ Project completion 80% ≥ 60% → eligible
- Years of service: 6 → +12% (6 × 2%)
- Role (Employee): +1%
- **Total increase: 13%**
- **New salary: $52,000 + $6,760 = $58,760**

---

## 📮 How to Test with Postman

### Process Employees

| Setting     | Value                                         |
|-------------|-----------------------------------------------|
| **Method**  | `POST`                                        |
| **URL**     | `http://localhost:8080/api/employees/process` |
| **Body**    | None required                                 |
| **Headers** | Default (no special headers needed)           |

### Steps:
1. Open **Postman**
2. Create a new request
3. Set method to **POST**
4. Enter URL: `http://localhost:8080/api/employees/process`
5. Click **Send**

### Expected Response (JSON):
```json
{
    "employees": [
        {
            "id": 1,
            "name": "Alice",
            "originalSalary": 52000.0,
            "newSalary": 58760.0,
            "totalIncreasePercentage": 13.0,
            "role": "Employee",
            "joinedDate": "2019-05-12",
            "yearsOfService": 6,
            "projectCompletionPercentage": 80.0,
            "eligible": true,
            "processedByThread": "pool-1-thread-1"
        },
        {
            "id": 4,
            "name": "Diana",
            "originalSalary": 60000.0,
            "newSalary": 60000.0,
            "totalIncreasePercentage": 0.0,
            "role": "Manager",
            "joinedDate": "2018-11-05",
            "yearsOfService": 7,
            "projectCompletionPercentage": 50.0,
            "eligible": false,
            "processedByThread": "pool-1-thread-3"
        }
    ]
}
```

> **Note:** The order of employees in the response may vary between runs because they are processed concurrently by different threads. The `processedByThread` field shows which thread handled each employee.

---

## 📂 CSV File

The sample CSV file is located at `src/data/test_employees.csv` with 30 employees.

**Format** (no header row):
```
id,name,salary,joinedDate,role,projectCompletionPercentage
1,Alice,52000.0,2019-05-12,Employee,0.8
2,Bob,68000.0,2020-08-23,Manager,0.6
...
```

> The `projectCompletionPercentage` is stored as a decimal (0.0–1.0) in the CSV and automatically converted to a percentage (0–100) by MapStruct during mapping.

---
