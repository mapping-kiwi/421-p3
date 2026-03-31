--#SET TERMINATOR @
-- Once executed in DB2, this procedure can be called from Java as
-- GetEligibleEmployees(slot_id).

CREATE PROCEDURE GetEligibleEmployees(IN selected_slot_id INT)
DYNAMIC RESULT SETS 1
LANGUAGE SQL
BEGIN
    DECLARE cursor1 CURSOR WITH RETURN FOR
        SELECT E.name, E.employee_no
        FROM EMPLOYEE E
        JOIN OCCUPIES O
            ON E.employee_no = O.employee_no
        JOIN CALENDARSLOT S_TARGET
            ON S_TARGET.slot_id = selected_slot_id
        WHERE O.service_id = S_TARGET.service_id
          AND EXISTS (
              SELECT 1
              FROM EMPLOYEEPOSITION EP
              WHERE EP.service_id = S_TARGET.service_id
          )
          AND NOT EXISTS (
              SELECT 1
              FROM ISASSIGNED A
              JOIN CALENDARSLOT S_BUSY
                  ON A.slot_id = S_BUSY.slot_id
              WHERE A.employee_no = E.employee_no
                AND S_BUSY.date = S_TARGET.date
                AND NOT (
                    S_BUSY.end_time <= S_TARGET.start_time
                    OR S_BUSY.start_time >= S_TARGET.end_time
                )
          );

    OPEN cursor1;
END @
