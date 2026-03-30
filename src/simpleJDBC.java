import java.sql.* ;
import java.sql.Date;
import java.util.*;


class simpleJDBC
{
    public static void main ( String [ ] args ) throws SQLException
    {
      // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        if ( args.length > 0 )
            tableName += args [ 0 ] ;
        else
          tableName += "exampletbl";

        // Register the driver.  You must register the driver before you can use it.
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 JDBC driver not found on the runtime classpath.");
            return;
        }

        String url = "jdbc:db2://winter2026-comp421.cs.mcgill.ca:50000/comp421";

        //DONT PUT THE USER AND PASSWORD ON GITHUB PLS
        String your_userid = System.getenv("DB421_USER");
        String your_password = System.getenv("DB421_PASSWORD");
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement statement = con.createStatement ( ) ;

        /// INSERT EXAMPLE
//        try
//        {
//          String insertSQL = "INSERT INTO " + tableName + " VALUES ( 1 , \'Vicki\' ) " ;
//          System.out.println ( insertSQL ) ;
//          statement.executeUpdate ( insertSQL ) ;
//          System.out.println ( "DONE" ) ;
//
//          insertSQL = "INSERT INTO " + tableName + " VALUES ( 2 , \'Vera\' ) " ;
//          System.out.println ( insertSQL ) ;
//          statement.executeUpdate ( insertSQL ) ;
//          System.out.println ( "DONE" ) ;
//          insertSQL = "INSERT INTO " + tableName + " VALUES ( 3 , \'Franca\' ) " ;
//          System.out.println ( insertSQL ) ;
//          statement.executeUpdate ( insertSQL ) ;
//          System.out.println ( "DONE" ) ;
//
//        }
//        catch (SQLException e)
//        {
//          sqlCode = e.getErrorCode(); // Get SQLCODE
//          sqlState = e.getSQLState(); // Get SQLSTATE
//
//          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
//          System.out.println(e);
//        }

        ///  Query Example
//        try
//        {
//          String querySQL = "SELECT BOOKING_ID, AMOUNT FROM BOOKING";
//          System.out.println (querySQL) ;
//          java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
//
//          while ( rs.next ( ) )
//          {
//            int id = rs.getInt ( 1 ) ;
//            String amt = rs.getString (2);
//            System.out.println ("id:  " + id);
//            System.out.println ("amount:  " + amt);
//          }
//         System.out.println ("DONE");
//        }
//        catch (SQLException e)
//        {
//          sqlCode = e.getErrorCode(); // Get SQLCODE
//          sqlState = e.getSQLState(); // Get SQLSTATE
//
//          // Your code to handle errors comes here;
//          // something more meaningful than a print would be good
//          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
//          System.out.println(e);
//        }

      ///  Update Table Example
//      try
//      {
//        String updateSQL = "UPDATE " + tableName + " SET NAME = \'Mimi\' WHERE id = 3";
//        System.out.println(updateSQL);
//        statement.executeUpdate(updateSQL);
//        System.out.println("DONE");
//
//        // Dropping a table
//        String dropSQL = "DROP TABLE " + tableName;
//        System.out.println ( dropSQL ) ;
//        statement.executeUpdate ( dropSQL ) ;
//        System.out.println ("DONE");
//      }
//      catch (SQLException e)
//      {
//        sqlCode = e.getErrorCode(); // Get SQLCODE
//        sqlState = e.getSQLState(); // Get SQLSTATE
//
//        // Your code to handle errors comes here;
//        // something more meaningful than a print would be good
//        System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
//        System.out.println(e);
//      }

      /// Main Menu
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            // Display menu
            System.out.println("\n===== DataBass Dashboard =====");
            System.out.println("1) View and/or Process Booking Requests");
            System.out.println("2) Create a new booking");
            System.out.println("3) Assign Employee to a Booked Time Slot");
            System.out.println("4) Reschedule a Booking");
            System.out.println("5) View Operational Insights Dashboard");
            System.out.println("6) Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {

                case 1:
                    /// santi
                    processBookings(scanner,con);
                    break;

                case 2:
                    //elisha
                    createBooking(scanner,con)
                    break;

                case 3:
                    assignEmployeesToSlots(scanner, con);
                    break;

                case 4:
                    System.out.println("\n--- Rescheduling Booking ---");
                    rescheduleBooking(scanner, con);
                    break;

                case 5:
                    operationalInsights(scanner, con);
                    break;
                case 6:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 6);

        scanner.close();

      // Finally but importantly close the statement and connection
      statement.close ( ) ;
      con.close ( ) ;
    }


    //Menu option 1
    private static void processBookings(Scanner scanner, Connection con){
        System.out.println("\n--- View and/or Process Booking Requests ---");

        //step 1: query and show bookings that have "pending" as their status
        String querySQL = "SELECT * FROM BOOKING WHERE UPPER(BOOKING_STATUS) = 'PENDING' ";
        try {
            Statement statement = con.createStatement ( ) ;
            java.sql.ResultSet rs = statement.executeQuery(querySQL);
            Set<Integer> validBookingIds = printAndCapture(rs, "BOOKING_ID", Integer.class);

            //step 2: ask user what booking id to select
            System.out.print("Select BookingID: ");
            int selection = scanner.nextInt();
            while(!validBookingIds.contains(selection)){
                System.out.println("Invalid BookingID selected, Please enter a valid BookingId");
                selection = scanner.nextInt();
            }

            //step 2.5: show the booking again on the console for clarity
            querySQL = "SELECT * FROM BOOKING WHERE BOOKING_ID="+selection;
            rs = statement.executeQuery(querySQL);
            printResultSet(rs);

            //step 3: ask user what status to assign to the booking
            System.out.print("Select status to assign: \n1)Confirm\n2)Reject\n3)Reschedule\n0)Cancel operation\nselection: ");
            int secondSelection = scanner.nextInt();
            while(!validBookingIds.contains(selection)){
                System.out.println("Invalid status selected, Please enter a valid status");
                secondSelection = scanner.nextInt();
            }
            switch(secondSelection){
                case 1: //confirm
                    querySQL = "UPDATE BOOKING SET BOOKING_STATUS='APPROVED' WHERE BOOKING_ID="+selection;
                    break;
                case 2: //reject
                    querySQL = "UPDATE BOOKING SET BOOKING_STATUS='CANCELLED' WHERE BOOKING_ID="+selection;
                    break;
                case 3: //reschedule
                    querySQL = "UPDATE BOOKING SET BOOKING_STATUS='TO_RESCHEDULE' WHERE BOOKING_ID="+selection;
                    break;
                case 0: //quit from option
                    break;
            }
            if(secondSelection!=0) {
                statement.execute(querySQL);
                System.out.println("BookingID " + selection + " was successfully updated.");
            }
            else{
                System.out.println("No changes were made.");
            }

        }
        catch (SQLException e)
        {
            int sqlCode = e.getErrorCode(); // Get SQLCODE
            String sqlState = e.getSQLState(); // Get SQLSTATE

            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }

    // Menu option 2
    private static void createBooking(Scanner scanner, Connection con, Statement statement) {
        System.out.println("\n--- Creating New Booking ---");

        try {
            // 1 - Enter and validate customer with their ID
            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();
            scanner.nextLine();

            // SQL query - Fetch the Customer ID from Customer table
            String clientCheckSQL = "SELECT COUNT(*) FROM CUSTOMER WHERE CUSTOMER_ID = ?";
            PreparedStatement clientStmt = con.prepareStatement(clientCheckSQL);
            clientStmt.setInt(1, customerId);
            ResultSet clientRS = clientStmt.executeQuery();
            clientRS.next();

            // If customer is new, add them to the system with all appropriate information
            if (clientRS.getInt(1) == 0) {
                System.out.println("Customer not found. Creating new customer...");

                System.out.print("Enter customer name: ");
                String name = scanner.nextLine();

                System.out.print("Enter customer email: ");
                String email = scanner.nextLine();

                System.out.print("Enter customer phone: ");
                String phone = scanner.nextLine();

                // Insert the new customer's info in the Customer table
                String insertClientSQL = "INSERT INTO CUSTOMER (CUSTOMER_ID, NAME, EMAIL, PHONE) VALUES (?, ?, ?, ?)";
                PreparedStatement insertClientStmt = con.prepareStatement(insertClientSQL);
                insertClientStmt.setInt(1, customerId);
                insertClientStmt.setString(2, name);
                insertClientStmt.setString(3, email);
                insertClientStmt.setString(4, phone);
                insertClientStmt.executeUpdate();
                insertClientStmt.close();
                System.out.println("New customer created. Welcome to DataBASS!");
            }
            clientStmt.close();

            // 2 - Ask for desired date and find first available slot
            System.out.print("Enter desired date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine();

            PreparedStatement slotStmt = con.prepareStatement(
                    "SELECT SLOT_ID, DATE FROM CALENDARSLOT " +
                            "WHERE UPPER(STATUS) = 'FREE' AND DATE = ? " +
                            "FETCH FIRST 1 ROW ONLY");
            slotStmt.setDate(1, Date.valueOf(dateInput));
            ResultSet slotRS = slotStmt.executeQuery();

            if (!slotRS.next()) {
                System.out.println("No available slots on " + dateInput + ". Please try another date.");
                slotStmt.close();
                return;
            }

            int chosenSlotId = slotRS.getInt("SLOT_ID");
            Date bookingDate = slotRS.getDate("DATE");
            slotStmt.close();
            System.out.println("Slot " + chosenSlotId + " automatically assigned on " + bookingDate + ".");

            // 3 - Get amount
            System.out.print("Enter amount: ");
            int amount = scanner.nextInt();
            scanner.nextLine();

            // 4 - Get payment method
            System.out.print("Enter payment method (e.g. CASH, CREDIT): ");
            String paymentMethod = scanner.nextLine();

            // 5 - Check for conflicts
            PreparedStatement checkStmt = con.prepareStatement(
                    "SELECT COUNT(*) FROM BOOKING WHERE CUSTOMER_ID = ? AND BOOKING_DATE = ?");
            checkStmt.setInt(1, customerId);
            checkStmt.setDate(2, bookingDate);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                System.out.println("This customer already has a booking on " + bookingDate + ".");
                System.out.println("Existing bookings:");
                PreparedStatement altStmt = con.prepareStatement(
                        "SELECT BOOKING_ID, STATUS, AMOUNT FROM BOOKING " +
                                "WHERE CUSTOMER_ID = ? AND BOOKING_DATE = ?");
                altStmt.setInt(1, customerId);
                altStmt.setDate(2, bookingDate);
                ResultSet altRS = altStmt.executeQuery();
                System.out.printf("  %-12s %-10s %-8s%n", "BookingID", "Status", "Amount");
                while (altRS.next()) {
                    System.out.printf("  %-12d %-10s %-8d%n",
                            altRS.getInt(1), altRS.getString(2), altRS.getInt(3));
                }
                altStmt.close();

            } else {
                // 6 - Get next available BOOKING_ID
                ResultSet maxRS = statement.executeQuery("SELECT MAX(BOOKING_ID) FROM BOOKING");
                maxRS.next();
                int nextBookingId = maxRS.getInt(1) + 1;

                // 7 - Insert the booking
                String insertSQL = "INSERT INTO BOOKING " +
                        "(BOOKING_ID, CUSTOMER_ID, BOOKING_DATE, AMOUNT, PAYMENT_METHOD, STATUS) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = con.prepareStatement(insertSQL);
                insertStmt.setInt(1, nextBookingId);
                insertStmt.setInt(2, customerId);
                insertStmt.setDate(3, bookingDate);
                insertStmt.setInt(4, amount);
                insertStmt.setString(5, paymentMethod);
                insertStmt.setString(6, "PENDING");
                insertStmt.executeUpdate();
                insertStmt.close();
                System.out.println("Booking " + nextBookingId + " created successfully.");
            }

            checkStmt.close();

        } catch (SQLException e) {
            int sqlCode = e.getErrorCode();
            String sqlState = e.getSQLState();
            System.out.println("SQL Error — Code: " + sqlCode + "  State: " + sqlState);
            System.out.println(e.getMessage());
        }
    }

    //Menu option 3
    private static void assignEmployeesToSlots(Scanner scanner, Connection con) {        
        System.out.println("\n--- Assign Employee to a Booked Time Slot ---");
        
        try {        
            Statement statement = con.createStatement ( ) ;

            //1. Display unassigned booked slots offering EmployeePosition services
            String unassigned_slots = 
                "SELECT * FROM CalendarSlot C " +
                //Service is an EmployeePosition. USING tells DB to merge duplicate service_id columns into one.
                "JOIN EmployeePosition E USING (service_id) " +
                //Booked 
                "WHERE slot_status = 'TAKEN' " +
                //Not already assigned to an employee
                "AND slot_id NOT IN (SELECT slot_id FROM isAssigned)"
                ;
            java.sql.ResultSet rs_unassigned_slots = statement.executeQuery(unassigned_slots);
            Set<Integer> unassigned_slot_ids = printAndCapture(rs_unassigned_slots, "SLOT_ID", Integer.class);

            //2. Ask user to select a slot_id
            System.out.println("Select SlotID: ");
            int slot_selected = scanner.nextInt();
            //Make sure it exists
            while(!unassigned_slot_ids.contains(slot_selected)){
                System.out.println("Invalid SlotID selected. Please enter a valid SlotID.");
                slot_selected = scanner.nextInt();
            }
            
            //3. Retrieve available employees by calling stored procedure GetEligible Employees
            CallableStatement cstmt_employees = con.prepareCall("{call GetEligibleEmployees(?)}");
            cstmt_employees.setInt(1,slot_selected);
            cstmt_employees.execute();
            java.sql.ResultSet rs_eligible_employees = cstmt_employees.getResultSet();
            //Display them as a table
            Set<Integer> available_employee_nos = printAndCapture(rs_eligible_employees, "EMPLOYEE_NO", Integer.class);

            //4. Ask user to select an employee to assign to the slot
            System.out.println("Select EmployeeNo of employee to assign: ");
            int employeeNo_selected = scanner.nextInt();
            //Make sure it exists
            while(!available_employee_nos.contains(employeeNo_selected)){
                System.out.println("Invalid EmployeeNo selected. Please enter a valid EmployeeNo.");
                employeeNo_selected = scanner.nextInt();
            }

            //5. Assign employee to slot in DB isAssigned table
            String assignment = "INSERT INTO isAssigned VALUES (?,?)";
            //Bind ? placeholders to their values
            PreparedStatement pstmt_assignment = con.prepareStatement(assignment);
            pstmt_assignment.setInt(1, slot_selected);
            pstmt_assignment.setInt(2, employeeNo_selected);
            pstmt_assignment.executeUpdate();

            //6. Display all the employee's assigned slots
            System.out.println("Current assignments for Employee " + employeeNo_selected + ": ");
            String selected_employee_assigns = "SELECT * FROM isAssigned WHERE employee_no = ?";
            PreparedStatement pstmt_view = con.prepareStatement(selected_employee_assigns);
            pstmt_view.setInt(1, employeeNo_selected);
            printResultSet(pstmt_view.executeQuery());

            statement.close ( ) ;
        }
        catch (SQLException e)
        {
            int sqlCode = e.getErrorCode(); 
            String sqlState = e.getSQLState(); 
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }

    //Menu Option 4
    private static void rescheduleBooking(Scanner scanner, Connection con){
        System.out.println("\n--- Reschedule Booking ---");
        //step 1: read booking Id

        int bookingId;
        while (true) {
            try {
                System.out.println("Enter Booking ID: ");
                bookingId = scanner.nextInt();
                scanner.nextLine();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric booking ID.");
                scanner.nextLine();
            }
        }
        //step 2: check whether the booking exists

        try {
            String bookingCheckSQL = "SELECT COUNT(*) FROM BOOKING WHERE BOOKING_ID = ?";
            PreparedStatement bookingCheckStmt = con.prepareStatement(bookingCheckSQL);
            bookingCheckStmt.setInt(1, bookingId);
            ResultSet rsBookingCheck = bookingCheckStmt.executeQuery();

            rsBookingCheck.next();
            int bookingCount = rsBookingCheck.getInt(1);

            //step 3: re-prompt if booking DNE
            //because the user might type a number that is numeric but invalid
            while (bookingCount == 0){
                while (true){
                    try {
                        System.out.println("Booking ID not found. Enter a valid booking ID: ");
                        bookingId = scanner.nextInt();
                        scanner.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a numeric booking ID.");
                        scanner.nextLine();
                    }
                }
                bookingCheckStmt.setInt(1, bookingId);
                rsBookingCheck = bookingCheckStmt.executeQuery();
                rsBookingCheck.next();
                bookingCount = rsBookingCheck.getInt(1);
            }

            //step 4: fetch current slot and service info

            String currentBookingSQL =
                    "SELECT F.SLOT_ID, CS.SERVICE_ID, CS.DATE, CS.START_TIME, CS.END_TIME " +
                            "FROM FILLS F " +
                            "JOIN CALENDARSLOT CS ON F.SLOT_ID = CS.SLOT_ID " +
                            "WHERE F.BOOKING_ID = ?";
            PreparedStatement currentBookingStmt = con.prepareStatement(currentBookingSQL);
            currentBookingStmt.setInt(1, bookingId);
            ResultSet rsCurrentBooking = currentBookingStmt.executeQuery();

            //important check because: A booking may exist in BOOKING, but if there is no row in FILLS, then it has no slot to reschedule.
            if (!rsCurrentBooking.next()){
                System.out.println("Booking exists but is not linked to any slot.");
                bookingCheckStmt.close();
                currentBookingStmt.close();
                return;
            }

            int currentSlotId = rsCurrentBooking.getInt("SLOT_ID");
            int currentServiceId = rsCurrentBooking.getInt("SERVICE_ID");
            Date currentDate = rsCurrentBooking.getDate("DATE");
            Time currentStartTime = rsCurrentBooking.getTime("START_TIME");
            Time currentEndTime = rsCurrentBooking.getTime("END_TIME");

            //step 5: show current booking details:
            System.out.println("\nCurrent booking details:");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Current Slot ID: " + currentSlotId);
            System.out.println("Service ID: " + currentServiceId);
            System.out.println("Date: " + currentDate);
            System.out.println("Time: " + currentStartTime + " - " + currentEndTime);

            //step 6: find alternative slots:
            String alternativesSQL =
                    "SELECT SLOT_ID, DATE, START_TIME, END_TIME " +
                            "FROM CALENDARSLOT " +
                            "WHERE SERVICE_ID = ? " +
                            "AND SLOT_STATUS = 'FREE' " +
                            "AND SLOT_ID <> ? " +
                            "AND DATE >= CURRENT_DATE " +
                            "ORDER BY DATE, START_TIME";

            PreparedStatement alternativesStmt = con.prepareStatement(alternativesSQL);
            alternativesStmt.setInt(1, currentServiceId);
            alternativesStmt.setInt(2, currentSlotId);
            ResultSet rsAlternatives = alternativesStmt.executeQuery();


            System.out.println("\nAvailable alternative slots:");
            Set<Integer> availableSlotIds = printAndCapture(rsAlternatives, "SLOT_ID", Integer.class);

            //step 7: handle 'no alternatives found'

            if (availableSlotIds.isEmpty()) {
                System.out.println("No alternative slots are available for this booking.");
                System.out.print("Would you like to mark this booking as TO_RESCHEDULE? (yes/no): ");
                String answer = scanner.nextLine().trim().toUpperCase();

                if (answer.equals("YES")) {
                    String markSQL = "UPDATE BOOKING SET BOOKING_STATUS = 'TO_RESCHEDULE' WHERE BOOKING_ID = ?";
                    PreparedStatement markStmt = con.prepareStatement(markSQL);
                    markStmt.setInt(1, bookingId);
                    markStmt.executeUpdate();
                    markStmt.close();

                    System.out.println("Booking marked as TO_RESCHEDULE.");
                } else {
                    System.out.println("No changes were made.");
                }

                bookingCheckStmt.close();
                currentBookingStmt.close();
                alternativesStmt.close();
                return;
            }

            //step 8: validate numeric input
            int newSlotId;

            while (true) {
                try {
                    System.out.print("Choose a replacement slot ID: ");
                    newSlotId = scanner.nextInt();
                    scanner.nextLine();
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a numeric slot ID.");
                    scanner.nextLine();
                }
            }

            //validate against actual DB-returned options:
            while (!availableSlotIds.contains(newSlotId)) {
                while (true) {
                    try {
                        System.out.print("Invalid slot ID. Enter a valid replacement slot ID: ");
                        newSlotId = scanner.nextInt();
                        scanner.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a numeric slot ID.");
                        scanner.nextLine();
                    }
                }
            }

            //step 9: apply the updates
            //old slot becomes free
            String freeOldSlotSQL = "UPDATE CALENDARSLOT SET SLOT_STATUS = 'FREE' WHERE SLOT_ID = ?";
            PreparedStatement freeOldSlotStmt = con.prepareStatement(freeOldSlotSQL);
            freeOldSlotStmt.setInt(1, currentSlotId);
            freeOldSlotStmt.executeUpdate();

            //new slot becomes taken
            String takeNewSlotSQL = "UPDATE CALENDARSLOT SET SLOT_STATUS = 'TAKEN' WHERE SLOT_ID = ?";
            PreparedStatement takeNewSlotStmt = con.prepareStatement(takeNewSlotSQL);
            takeNewSlotStmt.setInt(1, newSlotId);
            takeNewSlotStmt.executeUpdate();

            //update the booking-to-slot relationship
            String updateFillsSQL = "UPDATE FILLS SET SLOT_ID = ? WHERE BOOKING_ID = ?";
            PreparedStatement updateFillsStmt = con.prepareStatement(updateFillsSQL);
            updateFillsStmt.setInt(1, newSlotId);
            updateFillsStmt.setInt(2, bookingId);
            updateFillsStmt.executeUpdate();

            //step 10: success message and cleanup
            System.out.println("Booking " + bookingId + " successfully rescheduled to slot " + newSlotId + ".");

            bookingCheckStmt.close();
            currentBookingStmt.close();
            alternativesStmt.close();
            freeOldSlotStmt.close();
            takeNewSlotStmt.close();
            updateFillsStmt.close();

            //step 11: catch SQL exceptions
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode();
            String sqlState = e.getSQLState();
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }


    //Menu Option 5
    private static void operationalInsights(Scanner scanner, Connection con) {        
        System.out.println("\n--- Operational Insights ---");
        
        int nestedChoice;
        System.out.println("1) View Bookings By Service");
        System.out.println("2) View Studio Utilization");
        System.out.println("3) View Employee Workload");
        System.out.println("4) View Busiest Working Periods");
        System.out.print("Enter your choice: ");
        nestedChoice = scanner.nextInt();

        switch(nestedChoice){
            case 1:
                System.out.println("\n--- Bookings By Service ---");
                break;
            case 2:
                System.out.println("\n--- Studio Utilization ---");
                studioUtilization(scanner,con);
                break;
            case 3:
                employeeWorkload(scanner, con);
                break;
            case 4:
                System.out.println("\n--- Busy Working Periods ---");
                break;
            default:
                System.out.println("Invalid Option Selected");
        }
    }

    //Menu option 5 sub-option 2
    private static void studioUtilization(Scanner scanner, Connection con){

        try{
            //step 1: query our studios and store them in a hashset(?)
                //actually might be easier to have a hashmap (serviceId, roomID) for searching purposes
            String querySQL = "SELECT SERVICE_ID, ROOM_NO FROM STUDIOROOM";
            //HashSet<Integer> studioRooms = new HashSet<>();
            HashMap<Integer, Integer> studioRooms = new HashMap<>();
            Statement statement = con.createStatement();
            java.sql.ResultSet rs = statement.executeQuery(querySQL);
            while(rs.next()){
                int serviceId = rs.getInt("SERVICE_ID");
                int roomNo = rs.getInt("ROOM_NO");
                studioRooms.put(serviceId, roomNo);
            }

            //step 2: we should be able to query for a specific roomNo to see its utilization
            for(int key : studioRooms.keySet()){
                int offers = 0;
                int takenOffers = 0;
                querySQL = "SELECT * FROM CALENDARSLOT WHERE SERVICE_ID="+key;
                rs = statement.executeQuery(querySQL);
                while (rs.next()){
                    //free
                    offers+=1;
                    //taken
                    if(rs.getString("SLOT_STATUS").equals("TAKEN")){
                        takenOffers+=1;
                    }
                }

                double capacity = (offers == 0) ? 0 : (takenOffers * 100.0) / offers;
                System.out.printf(
                        "Studio Room %s is at %.2f%% capacity (offers: %d | taken: %d | free: %d)%n",
                        studioRooms.get(key),
                        capacity,
                        offers,
                        takenOffers,
                        (offers - takenOffers)
                );
            }

            System.out.print("Enter studio room nb: ");
            int key = scanner.nextInt();
            while(!studioRooms.values().contains(key))
            {
                System.out.println("Invalid studio nb entered, please try again: ");
                key = scanner.nextInt();
            }

            System.out.println("Select an option: \n1)See all slots\n2)See taken slots\n3)See all free slots\n0)Do Nothing");

            int selection = scanner.nextInt();
            while(selection<0 || selection>3){
                System.out.print("Invalid option chosen, try again: ");
                selection = scanner.nextInt();
            }

            querySQL = "SELECT SERVICE_ID FROM STUDIOROOM WHERE ROOM_NO="+key;
            rs = statement.executeQuery(querySQL);
            while(rs.next()) {
                key = rs.getInt("SERVICE_ID");
            }

            switch(selection){
                case 1:
                    querySQL = "SELECT * FROM CALENDARSLOT WHERE SERVICE_ID="+key;
                    break;
                case 2:
                    querySQL = "SELECT * FROM CALENDARSLOT WHERE SLOT_STATUS='TAKEN' AND SERVICE_ID="+key;
                    break;
                case 3:
                    querySQL = "SELECT * FROM CALENDARSLOT WHERE SLOT_STATUS='FREE' AND SERVICE_ID="+key;
                    break;
                case 0:
                    System.out.println("whoops");
                    break;
            }
            if(selection!=0) {
                rs = statement.executeQuery(querySQL);

                System.out.printf("%-10s %-12s %-12s %-12s %-12s %-12s%n",
                        "SLOT_ID", "SERVICE_ID", "DATE", "START_TIME", "END_TIME", "STATUS");
                System.out.println("--------------------------------------------------------------------------");

                while (rs.next()) {
                    System.out.printf("%-10d %-12d %-12s %-12s %-12s %-12s%n",
                            rs.getInt("SLOT_ID"),
                            rs.getInt("SERVICE_ID"),
                            rs.getString("DATE"),
                            rs.getString("START_TIME"),
                            rs.getString("END_TIME"),
                            rs.getString("SLOT_STATUS")
                    );
                }
            }
            else{
                System.out.println("No changes were made.");
            }
            //1st: get how often it is offered (count available and used)
            //2nd: get how many times it actually is used

        } catch (SQLException e) {
            int sqlCode = e.getErrorCode();
            String sqlState = e.getSQLState();

            System.out.println("SQL Error — Code: " + sqlCode + "  State: " + sqlState);
            System.out.println(e.getMessage());
        }
    }

    //Menu option 5 sub-option 3
    private static void employeeWorkload(Scanner scanner, Connection con) {
        System.out.println("\n--- Employee Workload ---");

        try {
            Statement statement = con.createStatement ( ) ;
            
            //DB optimizer sees I am joining Employee to isAssigned using employee_no --> uses the INDEX instead of searching the whole table row by row
            String employee_workload = 
                "SELECT E.name, COUNT(A.slot_id) AS total_bookings, " +
                    "SUM(HOUR(S.end_time - S.start_time)) AS total_hours " +
                "FROM Employee E " +
                "JOIN isAssigned A ON E.employee_no = A.employee_no " +
                "JOIN CalendarSlot S ON A.slot_id = S.slot_id " +
                "GROUP BY E.name";
                ;
            java.sql.ResultSet rs_employee_workload = statement.executeQuery(employee_workload);
            printResultSet(rs_employee_workload);

            statement.close();
        }

        catch (SQLException e)
        {
            int sqlCode = e.getErrorCode(); 
            String sqlState = e.getSQLState(); 
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }        
    }

    // HELPER FUNCTIONS we can all use

    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        // Print column headers with fixed width (e.g., 18 characters)
        for (int i = 1; i <= columnsNumber; i++) {
            System.out.printf("%-18s", rsmd.getColumnName(i));
        }
        System.out.println("\n" + "-".repeat(columnsNumber * 18));

        // Print rows
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                System.out.printf("%-18s", rs.getString(i));
            }
            System.out.println();
        }
    }

    //Print table and populate a set of the attribute/column passed as a parameter
    public static <T> Set<T> printAndCapture(ResultSet rs, String targetColumn, Class<T> type) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        Set<T> attributeSet = new HashSet<>();

        // 1. Print Header
        for (int i = 1; i <= columnsNumber; i++) {
            System.out.printf("%-18s", rsmd.getColumnName(i).toUpperCase());
        }
        System.out.println("\n" + "-".repeat(70));

        // 2. Process Rows
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                String colName = rsmd.getColumnName(i);
                Object value = rs.getObject(i);

                // Print the value for the table display
                System.out.printf("%-18s", value);

                // If this is our target column, add it to our Set
                if (colName.equalsIgnoreCase(targetColumn) && value != null) {
                    // type.cast ensures the Object is actually of type T
                    attributeSet.add(type.cast(value));
                }
            }
            System.out.println();
        }
        
        return attributeSet;
    }
    
}
