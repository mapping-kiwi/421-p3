import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class simpleJDBC {
    private static final String DB_URL = "jdbc:db2://winter2026-comp421.cs.mcgill.ca:50000/comp421";
    private static final String MENU_HEADER = "\n===== DataBass Dashboard =====";

    public static void main(String[] args) {
        if (!loadDriver()) {
            return;
        }

        String userId = getCredential("DB421_USER", "SOCSUSER");
        String password = getCredential("DB421_PASSWORD", "SOCSPASSWD");

        if (userId == null || password == null) {
            System.err.println("Error: database credentials are not available in the environment.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, userId, password);
             Scanner scanner = new Scanner(System.in)) {
            runMainMenu(scanner, connection);
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Database bootstrap
    // -------------------------------------------------------------------------

    private static boolean loadDriver() {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 JDBC driver not found on the runtime classpath.");
            return false;
        }
    }

    private static String getCredential(String primaryEnvVar, String fallbackEnvVar) {
        String value = System.getenv(primaryEnvVar);
        if (value == null) {
            value = System.getenv(fallbackEnvVar);
        }
        return value;
    }

    // -------------------------------------------------------------------------
    // Main menu routing
    // -------------------------------------------------------------------------

    private static void runMainMenu(Scanner scanner, Connection connection) {
        int choice;

        do {
            printMainMenu();
            choice = readInt(scanner, "Enter your choice: ");

            switch (choice) {
                case 1:
                    processBookings(scanner, connection);
                    break;
                case 2:
                    createBooking(scanner, connection);
                    break;
                case 3:
                    assignEmployeesToSlots(scanner, connection);
                    break;
                case 4:
                    rescheduleBooking(scanner, connection);
                    break;
                case 5:
                    showOperationalInsightsMenu(scanner, connection);
                    break;
                case 6:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 6);
    }

    private static void printMainMenu() {
        System.out.println(MENU_HEADER);
        System.out.println("1) View and/or Process Booking Requests");
        System.out.println("2) Create a new booking");
        System.out.println("3) Assign Employee to a Booked Time Slot");
        System.out.println("4) Reschedule a Booking");
        System.out.println("5) View Operational Insights Dashboard");
        System.out.println("6) Exit");
    }

    // -------------------------------------------------------------------------
    // Menu option 1: booking request processing
    // -------------------------------------------------------------------------

    private static void processBookings(Scanner scanner, Connection connection) {
        System.out.println("\n--- View and/or Process Booking Requests ---");

        String pendingBookingsSql = "SELECT * FROM BOOKING WHERE UPPER(BOOKING_STATUS) = 'PENDING'";
        String selectedBookingSql = "SELECT * FROM BOOKING WHERE BOOKING_ID = ?";

        try (Statement statement = connection.createStatement();
             ResultSet pendingBookings = statement.executeQuery(pendingBookingsSql)) {

            Set<Integer> validBookingIds = printAndCapture(pendingBookings, "BOOKING_ID", Integer.class);
            if (validBookingIds.isEmpty()) {
                System.out.println("There are no pending booking requests.");
                return;
            }

            int bookingId = promptForSelection(scanner, validBookingIds, "Select BookingID: ",
                    "Invalid BookingID selected. Please enter a valid BookingID.");

            try (PreparedStatement selectedBookingStatement = connection.prepareStatement(selectedBookingSql)) {
                selectedBookingStatement.setInt(1, bookingId);
                try (ResultSet selectedBooking = selectedBookingStatement.executeQuery()) {
                    printResultSet(selectedBooking);
                }
            }

            int statusChoice = promptForRange(
                    scanner,
                    "Select status to assign: \n1)Confirm\n2)Reject\n3)Reschedule\n0)Cancel operation\nselection: ",
                    0,
                    3,
                    "Invalid status selected. Please enter a valid status."
            );

            if (statusChoice == 0) {
                System.out.println("No changes were made.");
                return;
            }

            String updateSql;
            switch (statusChoice) {
                case 1:
                    updateSql = "UPDATE BOOKING SET BOOKING_STATUS='APPROVED' WHERE BOOKING_ID = ?";
                    break;
                case 2:
                    updateSql = "UPDATE BOOKING SET BOOKING_STATUS='CANCELLED' WHERE BOOKING_ID = ?";
                    break;
                case 3:
                    updateSql = "UPDATE BOOKING SET BOOKING_STATUS='TO_RESCHEDULE' WHERE BOOKING_ID = ?";
                    break;
                default:
                    System.out.println("No changes were made.");
                    return;
            }

            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setInt(1, bookingId);
                updateStatement.executeUpdate();
            }

            System.out.println("BookingID " + bookingId + " was successfully updated.");
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Menu option 2: booking creation
    // -------------------------------------------------------------------------

    private static void createBooking(Scanner scanner, Connection connection) {
        System.out.println("\n--- Creating New Booking ---");

        try {
            // Ask for customer ID once, before the loop
            int customerId = readInt(scanner, "Enter Customer ID: ");
            ensureCustomerExists(scanner, connection, customerId);

            while (true) {
                try {
                    System.out.print("Enter desired date (YYYY-MM-DD): ");
                    String dateInput = scanner.nextLine().trim();

                    // Check if date is in the past
                    Date today = new Date(System.currentTimeMillis());
                    Date desiredDate = Date.valueOf(dateInput);
                    if (desiredDate.before(today)) {
                        System.out.println("Cannot book a date in the past. Please try another date.");
                        continue;
                    }

                    Date bookingDate;
                    int chosenSlotId;

                    String slotSql =
                            "SELECT SLOT_ID, DATE FROM CALENDARSLOT " +
                                    "WHERE UPPER(SLOT_STATUS) = 'FREE' AND DATE = ? " +
                                    "FETCH FIRST 1 ROW ONLY";

                    try (PreparedStatement slotStatement = connection.prepareStatement(slotSql)) {
                        slotStatement.setDate(1, desiredDate);
                        try (ResultSet slotResult = slotStatement.executeQuery()) {
                            if (!slotResult.next()) {
                                System.out.println("No available slots on " + dateInput + ". Please try another date.");
                                continue;
                            }
                            chosenSlotId = slotResult.getInt("SLOT_ID");
                            bookingDate = slotResult.getDate("DATE");
                        }
                    }

                    System.out.println("Slot " + chosenSlotId + " automatically assigned on " + bookingDate + ".");

                    int amount = readInt(scanner, "Enter amount: ");
                    System.out.print("Enter payment method (e.g. CASH, CREDIT): ");
                    String paymentMethod = scanner.nextLine().trim();

                    if (customerHasBookingOnDate(connection, customerId, bookingDate)) {
                        printExistingCustomerBookings(connection, customerId, bookingDate);
                        continue;
                    }

                    int nextBookingId = getNextBookingId(connection);

                    String insertBookingSql =
                            "INSERT INTO BOOKING " +
                                    "(BOOKING_ID, CUSTOMER_ID, BOOKING_DATE, AMOUNT, PAYMENT_METHOD, BOOKING_STATUS) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement insertStatement = connection.prepareStatement(insertBookingSql)) {
                        insertStatement.setInt(1, nextBookingId);
                        insertStatement.setInt(2, customerId);
                        insertStatement.setDate(3, bookingDate);
                        insertStatement.setInt(4, amount);
                        insertStatement.setString(5, paymentMethod);
                        insertStatement.setString(6, "PENDING");
                        insertStatement.executeUpdate();
                    }

                    System.out.println("Booking " + nextBookingId + " created successfully.");
                    break;

                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                }
            }

        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    private static void ensureCustomerExists(Scanner scanner, Connection connection, int customerId) throws SQLException {
        String customerExistsSql = "SELECT COUNT(*) FROM CUSTOMER WHERE CUSTOMER_ID = ?";

        try (PreparedStatement customerCheck = connection.prepareStatement(customerExistsSql)) {
            customerCheck.setInt(1, customerId);

            try (ResultSet customerResult = customerCheck.executeQuery()) {
                customerResult.next();
                if (customerResult.getInt(1) > 0) {
                    return;
                }
            }
        }

        System.out.println("Customer not found. Creating new customer...");
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine();
        System.out.print("Enter customer phone: ");
        String phone = scanner.nextLine();

        String insertCustomerSql = "INSERT INTO CUSTOMER (CUSTOMER_ID, NAME, EMAIL, PHONE) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertCustomer = connection.prepareStatement(insertCustomerSql)) {
            insertCustomer.setInt(1, customerId);
            insertCustomer.setString(2, name);
            insertCustomer.setString(3, email);
            insertCustomer.setString(4, phone);
            insertCustomer.executeUpdate();
        }

        System.out.println("New customer created. Welcome to DataBASS!");
    }

    private static boolean customerHasBookingOnDate(Connection connection, int customerId, Date bookingDate)
            throws SQLException {
        String existingBookingSql = "SELECT COUNT(*) FROM BOOKING WHERE CUSTOMER_ID = ? AND BOOKING_DATE = ?";

        try (PreparedStatement checkStatement = connection.prepareStatement(existingBookingSql)) {
            checkStatement.setInt(1, customerId);
            checkStatement.setDate(2, bookingDate);

            try (ResultSet result = checkStatement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private static void printExistingCustomerBookings(Connection connection, int customerId, Date bookingDate)
            throws SQLException {
        System.out.println("This customer already has a booking on " + bookingDate + ".");
        System.out.println("Existing bookings:");

        String existingBookingsSql =
                "SELECT BOOKING_ID, BOOKING_STATUS, AMOUNT FROM BOOKING " +
                "WHERE CUSTOMER_ID = ? AND BOOKING_DATE = ?";

        try (PreparedStatement statement = connection.prepareStatement(existingBookingsSql)) {
            statement.setInt(1, customerId);
            statement.setDate(2, bookingDate);

            try (ResultSet result = statement.executeQuery()) {
                System.out.printf("  %-12s %-15s %-8s%n", "BookingID", "Status", "Amount");
                while (result.next()) {
                    System.out.printf(
                            "  %-12d %-15s %-8d%n",
                            result.getInt("BOOKING_ID"),
                            result.getString("BOOKING_STATUS"),
                            result.getInt("AMOUNT")
                    );
                }
            }
        }
    }

    private static int getNextBookingId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT MAX(BOOKING_ID) FROM BOOKING")) {
            result.next();
            return result.getInt(1) + 1;
        }
    }

    // -------------------------------------------------------------------------
    // Menu option 3: employee assignment
    // -------------------------------------------------------------------------

    private static void assignEmployeesToSlots(Scanner scanner, Connection connection) {
        System.out.println("\n--- Assign Employee to a Booked Time Slot ---");

        String unassignedSlotsSql =
                "SELECT * FROM CALENDARSLOT C " +
                "JOIN EMPLOYEEPOSITION E USING (service_id) " +
                "WHERE slot_status = 'TAKEN' " +
                "AND slot_id NOT IN (SELECT slot_id FROM ISASSIGNED)";

        try (Statement statement = connection.createStatement();
             ResultSet unassignedSlots = statement.executeQuery(unassignedSlotsSql)) {

            Set<Integer> unassignedSlotIds = printAndCapture(unassignedSlots, "SLOT_ID", Integer.class);
            if (unassignedSlotIds.isEmpty()) {
                System.out.println("There are no unassigned booked slots.");
                return;
            }

            int selectedSlotId = promptForSelection(
                    scanner,
                    unassignedSlotIds,
                    "Select SlotID: ",
                    "Invalid SlotID selected. Please enter a valid SlotID."
            );

            try (CallableStatement eligibleEmployeesCall = connection.prepareCall("{call GetEligibleEmployees(?)}")) {
                eligibleEmployeesCall.setInt(1, selectedSlotId);
                eligibleEmployeesCall.execute();

                try (ResultSet eligibleEmployees = eligibleEmployeesCall.getResultSet()) {
                    Set<Integer> availableEmployeeNos = printAndCapture(
                            eligibleEmployees,
                            "EMPLOYEE_NO",
                            Integer.class
                    );

                    if (availableEmployeeNos.isEmpty()) {
                        System.out.println("No eligible employees are available for this slot.");
                        return;
                    }

                    int selectedEmployeeNo = promptForSelection(
                            scanner,
                            availableEmployeeNos,
                            "Select EmployeeNo of employee to assign: ",
                            "Invalid EmployeeNo selected. Please enter a valid EmployeeNo."
                    );

                    String assignmentSql = "INSERT INTO ISASSIGNED VALUES (?, ?)";
                    try (PreparedStatement assignmentStatement = connection.prepareStatement(assignmentSql)) {
                        assignmentStatement.setInt(1, selectedSlotId);
                        assignmentStatement.setInt(2, selectedEmployeeNo);
                        assignmentStatement.executeUpdate();
                    }

                    System.out.println("Current assignments for Employee " + selectedEmployeeNo + ": ");
                    String employeeAssignmentsSql = "SELECT * FROM ISASSIGNED WHERE employee_no = ?";
                    try (PreparedStatement assignmentsView = connection.prepareStatement(employeeAssignmentsSql)) {
                        assignmentsView.setInt(1, selectedEmployeeNo);
                        try (ResultSet assignments = assignmentsView.executeQuery()) {
                            printResultSet(assignments);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Menu option 4: rescheduling
    // -------------------------------------------------------------------------

    private static void rescheduleBooking(Scanner scanner, Connection connection) {
        System.out.println("\n--- Reschedule Booking ---");

        int bookingId = readInt(scanner, "Enter Booking ID: ");

        String bookingExistsSql = "SELECT COUNT(*) FROM BOOKING WHERE BOOKING_ID = ?";
        String currentBookingSql =
                "SELECT F.SLOT_ID, CS.SERVICE_ID, CS.DATE, CS.START_TIME, CS.END_TIME " +
                "FROM FILLS F " +
                "JOIN CALENDARSLOT CS ON F.SLOT_ID = CS.SLOT_ID " +
                "WHERE F.BOOKING_ID = ?";
        String alternativesSql =
                "SELECT SLOT_ID, DATE, START_TIME, END_TIME " +
                "FROM CALENDARSLOT " +
                "WHERE SERVICE_ID = ? " +
                "AND SLOT_STATUS = 'FREE' " +
                "AND SLOT_ID <> ? " +
                "AND DATE >= CURRENT_DATE " +
                "ORDER BY DATE, START_TIME";

        try {
            while (!bookingExists(connection, bookingExistsSql, bookingId)) {
                bookingId = readInt(scanner, "Booking ID not found. Enter a valid booking ID: ");
            }

            try (PreparedStatement currentBookingStatement = connection.prepareStatement(currentBookingSql)) {
                currentBookingStatement.setInt(1, bookingId);

                try (ResultSet currentBooking = currentBookingStatement.executeQuery()) {
                    if (!currentBooking.next()) {
                        System.out.println("Booking exists but is not linked to any slot.");
                        return;
                    }

                    int currentSlotId = currentBooking.getInt("SLOT_ID");
                    int currentServiceId = currentBooking.getInt("SERVICE_ID");
                    Date currentDate = currentBooking.getDate("DATE");
                    Time currentStartTime = currentBooking.getTime("START_TIME");
                    Time currentEndTime = currentBooking.getTime("END_TIME");

                    printCurrentBookingDetails(
                            bookingId,
                            currentSlotId,
                            currentServiceId,
                            currentDate,
                            currentStartTime,
                            currentEndTime
                    );

                    try (PreparedStatement alternativesStatement = connection.prepareStatement(alternativesSql)) {
                        alternativesStatement.setInt(1, currentServiceId);
                        alternativesStatement.setInt(2, currentSlotId);

                        try (ResultSet alternatives = alternativesStatement.executeQuery()) {
                            System.out.println("\nAvailable alternative slots:");
                            Set<Integer> availableSlotIds = printAndCapture(alternatives, "SLOT_ID", Integer.class);

                            if (availableSlotIds.isEmpty()) {
                                handleNoAlternativeSlots(scanner, connection, bookingId);
                                return;
                            }

                            int newSlotId = promptForSelection(
                                    scanner,
                                    availableSlotIds,
                                    "Choose a replacement slot ID: ",
                                    "Invalid slot ID. Enter a valid replacement slot ID."
                            );

                            applyRescheduleUpdates(connection, bookingId, currentSlotId, newSlotId);
                            System.out.println(
                                    "Booking " + bookingId + " successfully rescheduled to slot " + newSlotId + "."
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    private static boolean bookingExists(Connection connection, String bookingExistsSql, int bookingId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(bookingExistsSql)) {
            statement.setInt(1, bookingId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private static void printCurrentBookingDetails(
            int bookingId,
            int currentSlotId,
            int currentServiceId,
            Date currentDate,
            Time currentStartTime,
            Time currentEndTime
    ) {
        System.out.println("\nCurrent booking details:");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Current Slot ID: " + currentSlotId);
        System.out.println("Service ID: " + currentServiceId);
        System.out.println("Date: " + currentDate);
        System.out.println("Time: " + currentStartTime + " - " + currentEndTime);
    }

    private static void handleNoAlternativeSlots(Scanner scanner, Connection connection, int bookingId)
            throws SQLException {
        System.out.println("No alternative slots are available for this booking.");
        System.out.print("Would you like to mark this booking as TO_RESCHEDULE? (yes/no): ");
        String answer = scanner.nextLine().trim().toUpperCase();

        if (!answer.equals("YES")) {
            System.out.println("No changes were made.");
            return;
        }

        String markBookingSql = "UPDATE BOOKING SET BOOKING_STATUS = 'TO_RESCHEDULE' WHERE BOOKING_ID = ?";
        try (PreparedStatement markStatement = connection.prepareStatement(markBookingSql)) {
            markStatement.setInt(1, bookingId);
            markStatement.executeUpdate();
        }

        System.out.println("Booking marked as TO_RESCHEDULE.");
    }

    private static void applyRescheduleUpdates(Connection connection, int bookingId, int currentSlotId, int newSlotId)
            throws SQLException {
        String freeOldSlotSql = "UPDATE CALENDARSLOT SET SLOT_STATUS = 'FREE' WHERE SLOT_ID = ?";
        String takeNewSlotSql = "UPDATE CALENDARSLOT SET SLOT_STATUS = 'TAKEN' WHERE SLOT_ID = ?";
        String updateFillsSql = "UPDATE FILLS SET SLOT_ID = ? WHERE BOOKING_ID = ?";

        try (PreparedStatement freeOldSlot = connection.prepareStatement(freeOldSlotSql);
             PreparedStatement takeNewSlot = connection.prepareStatement(takeNewSlotSql);
             PreparedStatement updateFills = connection.prepareStatement(updateFillsSql)) {

            freeOldSlot.setInt(1, currentSlotId);
            freeOldSlot.executeUpdate();

            takeNewSlot.setInt(1, newSlotId);
            takeNewSlot.executeUpdate();

            updateFills.setInt(1, newSlotId);
            updateFills.setInt(2, bookingId);
            updateFills.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Menu option 5: operational insights
    // -------------------------------------------------------------------------

    private static void showOperationalInsightsMenu(Scanner scanner, Connection connection) {
        System.out.println("\n--- Operational Insights ---");
        System.out.println("1) View Bookings By Service");
        System.out.println("2) View Studio Utilization");
        System.out.println("3) View Employee Workload");
        System.out.println("4) View Busiest Working Periods");
        System.out.println("5) Back");

        int choice = readInt(scanner, "Enter your choice: ");

        switch (choice) {
            case 1:
                System.out.println("\n--- Bookings By Service ---");
                showBookingsByService(connection);
                break;
            case 2:
                System.out.println("\n--- Studio Utilization ---");
                showStudioUtilization(scanner, connection);
                break;
            case 3:
                System.out.println("\n--- Employee Workload ---");
                showEmployeeWorkload(connection);
                break;
            case 4:
                System.out.println("\n--- Busy Working Periods ---");
                showBusiestPeriods(connection);
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid Option Selected");
        }
    }

    private static void showBookingsByService(Connection connection) {
        String querySql =
                "SELECT CS.service_id, COUNT(*) AS booking_count " +
                "FROM BOOKING B " +
                "JOIN FILLS F ON B.booking_id = F.booking_id " +
                "JOIN CALENDARSLOT CS ON F.slot_id = CS.slot_id " +
                "WHERE B.booking_status = 'APPROVED' " +
                "GROUP BY CS.service_id " +
                "ORDER BY booking_count DESC";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(querySql)) {
            printResultSet(result);
        } catch (SQLException e) {
            printSqlException(e);
        }
    }


    private static void showStudioUtilization(Scanner scanner, Connection connection) {
        String roomLookupSql = "SELECT SERVICE_ID, ROOM_NO FROM STUDIOROOM";

        try (Statement statement = connection.createStatement();
             ResultSet roomResult = statement.executeQuery(roomLookupSql)) {

            Map<Integer, Integer> studioRoomsByService = new HashMap<>();
            while (roomResult.next()) {
                studioRoomsByService.put(roomResult.getInt("SERVICE_ID"), roomResult.getInt("ROOM_NO"));
            }

            for (int serviceId : studioRoomsByService.keySet()) {
                int offers = 0;
                int takenOffers = 0;

                try (ResultSet slotResult = statement.executeQuery(
                        "SELECT * FROM CALENDARSLOT WHERE SERVICE_ID = " + serviceId)) {
                    while (slotResult.next()) {
                        offers += 1;
                        if ("TAKEN".equals(slotResult.getString("SLOT_STATUS"))) {
                            takenOffers += 1;
                        }
                    }
                }

                double capacity = offers == 0 ? 0 : (takenOffers * 100.0) / offers;
                System.out.printf(
                        "Studio Room %s is at %.2f%% capacity (offers: %d | taken: %d | free: %d)%n",
                        studioRoomsByService.get(serviceId),
                        capacity,
                        offers,
                        takenOffers,
                        offers - takenOffers
                );
            }

            int roomNumber = promptForStudioRoom(scanner, studioRoomsByService);
            int selection = promptForRange(
                    scanner,
                    "Select an option: \n1)See all slots\n2)See taken slots\n3)See all free slots\n0)Do Nothing\n",
                    0,
                    3,
                    "Invalid option chosen, try again: "
            );

            if (selection == 0) {
                System.out.println("No changes were made.");
                return;
            }

            int serviceId = getServiceIdForRoom(connection, roomNumber);
            String querySql = buildStudioSlotsQuery(selection, serviceId);

            try (ResultSet slots = statement.executeQuery(querySql)) {
                printStudioSlots(slots);
            }
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    private static int promptForStudioRoom(Scanner scanner, Map<Integer, Integer> studioRoomsByService) {
        Set<Integer> roomNumbers = new HashSet<>(studioRoomsByService.values());
        return promptForSelection(
                scanner,
                roomNumbers,
                "Enter studio room nb: ",
                "Invalid studio nb entered, please try again: "
        );
    }

    private static int getServiceIdForRoom(Connection connection, int roomNumber) throws SQLException {
        String querySql = "SELECT SERVICE_ID FROM STUDIOROOM WHERE ROOM_NO = ?";
        try (PreparedStatement statement = connection.prepareStatement(querySql)) {
            statement.setInt(1, roomNumber);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt("SERVICE_ID");
            }
        }
    }

    private static String buildStudioSlotsQuery(int selection, int serviceId) {
        switch (selection) {
            case 1:
                return "SELECT * FROM CALENDARSLOT WHERE SERVICE_ID = " + serviceId;
            case 2:
                return "SELECT * FROM CALENDARSLOT WHERE SLOT_STATUS = 'TAKEN' AND SERVICE_ID = " + serviceId;
            case 3:
                return "SELECT * FROM CALENDARSLOT WHERE SLOT_STATUS = 'FREE' AND SERVICE_ID = " + serviceId;
            default:
                return "";
        }
    }

    private static void printStudioSlots(ResultSet resultSet) throws SQLException {
        System.out.printf(
                "%-10s %-12s %-12s %-12s %-12s %-12s%n",
                "SLOT_ID",
                "SERVICE_ID",
                "DATE",
                "START_TIME",
                "END_TIME",
                "SLOT_STATUS"
        );
        System.out.println("--------------------------------------------------------------------------");

        while (resultSet.next()) {
            System.out.printf(
                    "%-10d %-12d %-12s %-12s %-12s %-12s%n",
                    resultSet.getInt("SLOT_ID"),
                    resultSet.getInt("SERVICE_ID"),
                    resultSet.getString("DATE"),
                    resultSet.getString("START_TIME"),
                    resultSet.getString("END_TIME"),
                    resultSet.getString("SLOT_STATUS")
            );
        }
    }

    private static void showEmployeeWorkload(Connection connection) {
        String employeeWorkloadSql =
                "SELECT E.name, COUNT(A.slot_id) AS total_bookings, " +
                "SUM(HOUR(S.end_time - S.start_time)) AS total_hours " +
                "FROM EMPLOYEE E " +
                "JOIN ISASSIGNED A ON E.employee_no = A.employee_no " +
                "JOIN CALENDARSLOT S ON A.slot_id = S.slot_id " +
                "GROUP BY E.name";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(employeeWorkloadSql)) {
            printResultSet(result);
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    private static void showBusiestPeriods(Connection connection) {
        String querySql =
                "SELECT " +
                "  CASE " +
                "    WHEN HOUR(START_TIME) BETWEEN 9 AND 11 THEN '09:00 - 12:00' " +
                "    WHEN HOUR(START_TIME) BETWEEN 12 AND 14 THEN '12:00 - 15:00' " +
                "    WHEN HOUR(START_TIME) BETWEEN 15 AND 17 THEN '15:00 - 18:00' " +
                "    ELSE 'Other' " +
                "  END AS TIME_PERIOD, " +
                "  COUNT(B.BOOKING_ID) AS NUM_BOOKINGS " +
                "FROM BOOKING B " +
                "JOIN FILLS F ON B.BOOKING_ID = F.BOOKING_ID " +
                "JOIN CALENDARSLOT CS ON F.SLOT_ID = CS.SLOT_ID " +
                "GROUP BY " +
                "  CASE " +
                "    WHEN HOUR(START_TIME) BETWEEN 9 AND 11 THEN '09:00 - 12:00' " +
                "    WHEN HOUR(START_TIME) BETWEEN 12 AND 14 THEN '12:00 - 15:00' " +
                "    WHEN HOUR(START_TIME) BETWEEN 15 AND 17 THEN '15:00 - 18:00' " +
                "    ELSE 'Other' " +
                "  END " +
                "ORDER BY COUNT(B.BOOKING_ID) DESC";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(querySql)) {
            System.out.printf("%-20s %-15s%n", "Time Period", "Num Bookings");
            System.out.println("------------------------------------");

            while (result.next()) {
                System.out.printf(
                        "%-20s %-15d%n",
                        result.getString("TIME_PERIOD"),
                        result.getInt("NUM_BOOKINGS")
                );
            }
        } catch (SQLException e) {
            printSqlException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            }

            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
        }
    }

    private static int promptForRange(
            Scanner scanner,
            String prompt,
            int minInclusive,
            int maxInclusive,
            String errorMessage
    ) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value >= minInclusive && value <= maxInclusive) {
                return value;
            }
            System.out.println(errorMessage);
        }
    }

    private static int promptForSelection(
            Scanner scanner,
            Set<Integer> validOptions,
            String prompt,
            String errorMessage
    ) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (validOptions.contains(value)) {
                return value;
            }
            System.out.println(errorMessage);
        }
    }

    private static void printSqlException(SQLException e) {
        System.out.println("Code: " + e.getErrorCode() + "  sqlState: " + e.getSQLState());
        System.out.println(e.getMessage());
    }

    public static void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-18s", metadata.getColumnName(i));
        }
        System.out.println("\n" + "-".repeat(columnCount * 18));

        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-18s", resultSet.getString(i));
            }
            System.out.println();
        }
    }

    public static <T> Set<T> printAndCapture(ResultSet resultSet, String targetColumn, Class<T> type)
            throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        Set<T> capturedValues = new HashSet<>();

        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-18s", metadata.getColumnName(i).toUpperCase());
        }
        System.out.println("\n" + "-".repeat(70));

        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metadata.getColumnName(i);
                Object value = resultSet.getObject(i);

                System.out.printf("%-18s", value);

                if (columnName.equalsIgnoreCase(targetColumn) && value != null) {
                    capturedValues.add(type.cast(value));
                }
            }
            System.out.println();
        }

        return capturedValues;
    }
}
