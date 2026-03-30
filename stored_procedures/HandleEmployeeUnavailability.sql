CREATE PROCEDURE HandleEmployeeUnavailability(
    IN p_employee_no INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    DECLARE v_done INT DEFAULT 0; -- when the cursor has no more rows
    DECLARE v_slot_id INT; -- current affected slot
    DECLARE v_service_id INT; -- needed to find qualified replacements
    DECLARE v_slot_date DATE; -- needed to check time overlap
    DECLARE v_start_time TIME; -- ^
    DECLARE v_end_time TIME; -- ^
    DECLARE v_replacement_employee INT DEFAULT NULL; -- holds the replacement if found
    DECLARE v_booking_id INT DEFAULT NULL; -- needed if we have to mark booking TO_RESCHEDULE

    -- set of affected slots assigned to the unavailable employee needed to be processed
    DECLARE affected_slots CURSOR FOR
        SELECT A.slot_id, C.service_id, C.date, C.start_time, C.end_time
        FROM ISASSIGNED A
        JOIN CALENDARSLOT C ON A.slot_id = C.slot_id
        WHERE A.employee_no = p_employee_no
          AND C.date BETWEEN p_start_date AND p_end_date;

    -- the cursor basically is looping over every slot affected by this unavailable employee
    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_done = 1; -- termination constraint

    IF p_start_date > p_end_date THEN
        SIGNAL SQLSTATE '75000'
            SET MESSAGE_TEXT = 'p_start_date must be on or before p_end_date';
    END IF;

    OPEN affected_slots; -- cursor to begin fetching rows

    slot_loop:
    LOOP
        -- fetch the next affected slot, store its values in vars, if no more rows -> leave loop
        FETCH affected_slots
        INTO v_slot_id, v_service_id, v_slot_date, v_start_time, v_end_time;

        IF v_done = 1 THEN
            LEAVE slot_loop;
        END IF;

        -- reset replacement variable each iteration
        SET v_replacement_employee = NULL;
        SET v_booking_id = NULL;

        -- if replacement exists, pick one valid employee automatically
        SELECT MIN(O.employee_no)
        INTO v_replacement_employee
        FROM OCCUPIES O
        WHERE O.service_id = v_service_id -- must be the same service
          AND O.employee_no <> p_employee_no -- make sure its not the same employee
          AND NOT EXISTS (
                SELECT 1
                FROM ISASSIGNED A2
                JOIN CALENDARSLOT C2 ON A2.slot_id = C2.slot_id
                WHERE A2.employee_no = O.employee_no
                  AND C2.date = v_slot_date
                  AND NOT (
                        C2.end_time <= v_start_time
                        OR C2.start_time >= v_end_time
                  )
          );

        -- success branch
        IF v_replacement_employee IS NOT NULL THEN
            UPDATE ISASSIGNED
            SET employee_no = v_replacement_employee
            WHERE slot_id = v_slot_id
              AND employee_no = p_employee_no;

        -- if no replacement found, flag the booking
        ELSE
            SELECT MIN(F.booking_id)
            INTO v_booking_id
            FROM FILLS F
            WHERE F.slot_id = v_slot_id;

            IF v_booking_id IS NOT NULL THEN
                UPDATE BOOKING
                SET booking_status = 'TO_RESCHEDULE'
                WHERE booking_id = v_booking_id;
            END IF;
        END IF;

    END LOOP;

    CLOSE affected_slots;
END
@
