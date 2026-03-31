-- =========================================================
-- TEST SETUP FOR HandleEmployeeUnavailability
-- Group 53 - DataBASS Studios
--
-- Purpose:
-- Creates a controlled scenario where:
-- 1. employee 3 is assigned to two slots
-- 2. slot 9001 has a valid replacement employee (7)
-- 3. slot 9002 has no valid replacement
-- 4. booking 8001 should remain APPROVED
-- 5. booking 8002 should become TO_RESCHEDULE
-- =========================================================


-- -------------------------
-- Cleanup old test data
-- -------------------------
DELETE FROM FILLS
WHERE booking_id IN (8001, 8002);

DELETE FROM ISASSIGNED
WHERE slot_id IN (9001, 9002);

DELETE FROM BOOKING
WHERE booking_id IN (8001, 8002);

DELETE FROM CALENDARSLOT
WHERE slot_id IN (9001, 9002);

DELETE FROM OCCUPIES
WHERE employee_no IN (3, 7)
  AND service_id IN (20, 21);

DELETE FROM EMPLOYEE
WHERE employee_no IN (3, 7);

DELETE FROM EMPLOYEEPOSITION
WHERE service_id IN (20, 21);

DELETE FROM SERVICE
WHERE service_id IN (20, 21);

DELETE FROM CUSTOMER
WHERE customer_id = 5001;


-- -------------------------
-- Insert services
-- -------------------------
INSERT INTO SERVICE (service_id, hourly_rate)
VALUES (20, 100);

INSERT INTO SERVICE (service_id, hourly_rate)
VALUES (21, 120);


-- -------------------------
-- Insert employee positions
-- LEVEL is numeric in your schema
-- -------------------------
INSERT INTO EMPLOYEEPOSITION (service_id, title, level)
VALUES (20, 'Recording Engineer', 2);

INSERT INTO EMPLOYEEPOSITION (service_id, title, level)
VALUES (21, 'Mixing Engineer', 2);


-- -------------------------
-- Insert employees
-- employee 3 = unavailable employee
-- employee 7 = valid replacement for service 20 only
-- -------------------------
INSERT INTO EMPLOYEE (employee_no, name)
VALUES (3, 'Unavailable Employee');

INSERT INTO EMPLOYEE (employee_no, name)
VALUES (7, 'Replacement Employee');


-- -------------------------
-- Insert service qualifications
-- employee 3 can do both services
-- employee 7 can only do service 20
-- -------------------------
INSERT INTO OCCUPIES (employee_no, service_id)
VALUES (3, 20);

INSERT INTO OCCUPIES (employee_no, service_id)
VALUES (3, 21);

INSERT INTO OCCUPIES (employee_no, service_id)
VALUES (7, 20);


-- -------------------------
-- Insert customer
-- -------------------------
INSERT INTO CUSTOMER (customer_id, name, email, phone)
VALUES (5001, 'Test Customer', 'test@example.com', '5140000000');


-- -------------------------
-- Insert calendar slots
-- slot 9001 -> service 20 -> should be reassigned
-- slot 9002 -> service 21 -> should fail reassignment
-- -------------------------
INSERT INTO CALENDARSLOT (slot_id, service_id, date, start_time, end_time, slot_status)
VALUES (9001, 20, '2026-01-15', '10:00:00', '11:00:00', 'TAKEN');

INSERT INTO CALENDARSLOT (slot_id, service_id, date, start_time, end_time, slot_status)
VALUES (9002, 21, '2026-01-16', '12:00:00', '13:00:00', 'TAKEN');


-- -------------------------
-- Insert bookings
-- both begin as APPROVED
-- -------------------------
INSERT INTO BOOKING (booking_id, booking_date, customer_id, amount, booking_status, payment_method)
VALUES (8001, '2026-01-10', 5001, 100, 'APPROVED', 'CARD');

INSERT INTO BOOKING (booking_id, booking_date, customer_id, amount, booking_status, payment_method)
VALUES (8002, '2026-01-10', 5001, 120, 'APPROVED', 'CARD');


-- -------------------------
-- Link bookings to slots
-- -------------------------
INSERT INTO FILLS (booking_id, slot_id)
VALUES (8001, 9001);

INSERT INTO FILLS (booking_id, slot_id)
VALUES (8002, 9002);


-- -------------------------
-- Assign unavailable employee 3 to both slots
-- -------------------------
INSERT INTO ISASSIGNED (slot_id, employee_no)
VALUES (9001, 3);

INSERT INTO ISASSIGNED (slot_id, employee_no)
VALUES (9002, 3);


