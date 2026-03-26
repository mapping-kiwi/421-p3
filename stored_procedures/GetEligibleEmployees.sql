--Once I execute this code in our DB, we will be able to call the procedure within the Java code as GetEligibleEmployees(slot_id).

CREATE PROCEDURE GetEligibleEmployees (IN selected_slot_id INT)
DYNAMIC RESULT SETS 1
BEGIN
    DECLARE cursor1 CURSOR WITH RETURN FOR

    SELECT E.name, E.employee_no
    FROM Employee E
    -- 1. Ensure employee has the right specialty
    JOIN occupies O ON E.employee_no = O.employee_no
    JOIN CalendarSlot S_Target ON S_Target.slot_id = selected_slot_id
    WHERE O.service_id = S_Target.service_id

    -- 2. Ensure they aren't busy during timeslot
    AND E.employee_no NOT IN (
        SELECT A.employee_no
        FROM isAssigned A
        JOIN CalendarSlot S_Busy ON A.slot_id = S_Busy.slot_id
        -- Same day
        WHERE S_Busy.date = S_Target.date
        -- Overlapping times
        AND NOT (S_Busy.end_time <= S_Target.start_time OR S_Busy.start_time >= S_Target.end_time)
    );

    OPEN cursor1;
END @