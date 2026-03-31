# DataBASS Studios Database Application
COMP 421 – Project 3 (Group 53)

## Overview
This project implements a full-stack database application for managing a music studio booking system. It integrates a DB2 relational database with a Java-based command-line interface to handle bookings, staff assignment, scheduling, and operational analytics.

The system is designed to simulate real-world studio operations, including customer bookings, employee scheduling, and business insights.

---

## Features

### Core Functionality
- View and process booking requests (approve, reject, reschedule)
- Create new bookings with conflict checks
- Assign qualified employees to booked time slots
- Reschedule bookings with intelligent slot suggestions

### Operational Insights Dashboard
- Bookings by service (popularity analysis)
- Studio utilization (capacity tracking)
- Employee workload (assignment + hours)
- Busiest working periods (time-based demand)

---

## Technologies Used

- **Java (JDBC)** – application logic and user interface
- **IBM DB2** – relational database backend
- **SQL (DDL/DML)** – schema, queries, stored procedures
- **Stored Procedures** – automation of scheduling logic

---


## Key Components

### Stored Procedures
- **HandleEmployeeUnavailability**
    - Reassigns employees for affected bookings
    - Flags bookings for rescheduling if no replacement is available

- **GetEligibleEmployees**
    - Returns available and qualified employees for a given time slot

---

### Indexing
- Index created on `ISASSIGNED(employee_no, slot_id)`
- Optimizes employee workload and assignment queries

---

### Sample Analytical Query

```sql
SELECT CS.service_id, COUNT(*) AS booking_count
FROM BOOKING B
JOIN FILLS F ON B.booking_id = F.booking_id
JOIN CALENDARSLOT CS ON F.slot_id = CS.slot_id
WHERE B.booking_status = 'APPROVED'
GROUP BY CS.service_id
ORDER BY booking_count DESC;
   ```

How to run:
javac -d out src/simpleJDBC.java







# DataBASS Studios Database Application  
COMP 421 – Project 3 (Group 53)

## Overview
This project implements a full-stack database application for managing a music studio booking system. It integrates a DB2 relational database with a Java-based command-line interface to handle bookings, staff assignment, scheduling, and operational analytics.

The system simulates real-world studio operations, including customer bookings, employee scheduling, and business insights.

---

## Features

### Core Functionality
- View and process booking requests (approve, reject, reschedule)
- Create new bookings with conflict checks
- Assign qualified employees to booked time slots
- Reschedule bookings with intelligent slot suggestions

### Operational Insights Dashboard
- Bookings by service (popularity analysis)
- Studio utilization (capacity tracking)
- Employee workload (assignment + hours)
- Busiest working periods (time-based demand)

---

## Technologies Used
- **Java (JDBC)** – application logic and CLI interface  
- **IBM DB2** – relational database backend  
- **SQL (DDL/DML)** – schema, queries, indexing  
- **Stored Procedures** – scheduling and automation logic  

---

## Project Structure

```
COMP421/
├── src/
│   └── simpleJDBC.java
│
├── stored_procedures/
│   ├── HandleEmployeeUnavailability.sql
│   ├── sp_GetEligibleEmployees.sql
│   ├── index.sql
│   └── test_setup.sql
│
└── P3_Report.pdf
```

---

## Key Components

### Stored Procedures
- **HandleEmployeeUnavailability**
  - Reassigns employees for affected bookings
  - Flags bookings for rescheduling if no replacement is available

- **GetEligibleEmployees**
  - Returns available and qualified employees for a given time slot

---

### Indexing
- Index created on:
```
ISASSIGNED(employee_no, slot_id)
```
- Improves performance of employee workload and assignment queries

---

### Sample Analytical Query

```sql
SELECT CS.service_id, COUNT(*) AS booking_count
FROM BOOKING B
JOIN FILLS F ON B.booking_id = F.booking_id
JOIN CALENDARSLOT CS ON F.slot_id = CS.slot_id
WHERE B.booking_status = 'APPROVED'
GROUP BY CS.service_id
ORDER BY booking_count DESC;
```

---

## How to Run

### Requirements
- Must be connected to McGill network (VPN if off-campus)
- Access to DB2 server: `winter2026-comp421.cs.mcgill.ca`
- DB2 JDBC driver (`db2jcc4.jar`)

### 1. Set Credentials

```
export DB421_USER=your_username
export DB421_PASSWORD=your_password
```

### 2. Compile

```
javac -d out src/simpleJDBC.java
```

### 3. Run

```
java -cp "out:/path/to/db2jcc4.jar" simpleJDBC
```

### 4. Setup Database (one-time)

```
db2 -td@ -vf stored_procedures/HandleEmployeeUnavailability.sql
db2 -td@ -vf stored_procedures/sp_GetEligibleEmployees.sql
db2 -vf stored_procedures/index.sql
db2 -vf stored_procedures/test_setup.sql
```

---

## Notes
- Requires connection to McGill DB2 server (VPN if off-campus)
- Ensure `db2jcc4.jar` is included in the classpath when running
- SQL scripts must be executed before using the application

---

## Authors
COMP 421 – Group 53  
DataBASS Studios Project