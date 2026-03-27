import com.ibm.db2.jcc.t4.ServerListEntry;

import java.sql.* ;
import java.util.*;
import java.util.stream.Collectors;

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
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }


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
            System.out.println("3) Assign Staff to a Booking");
            System.out.println("4) Reschedule a Booking");
            System.out.println("5) View Operational Insights Dashboard");
            System.out.println("6) Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            //switch vars
            String date;
            String service;
            int booking_id;
            String querySQL;

            switch (choice) {

                case 1:
                    /// santi
                    System.out.println("\n--- View and/or Process Booking Requests ---");

                    System.out.printf("%-12s %-12s %-8s %-10s %-15s%n",
                            "BookingID", "Date", "Amount", "Status", "Payment");
                    System.out.println("-------------------------------------------------------------");

                    //step 1: query and show bookings that have "pending" as their status
                    querySQL = "SELECT * FROM BOOKING WHERE UPPER(STATUS) = 'PENDING' ";
                    HashSet<Integer> validBookingIds = new HashSet<>();
                    try {
                        java.sql.ResultSet rs = statement.executeQuery(querySQL);
                        while( rs.next() ){
                            int bookingId = rs.getInt("BOOKING_ID");
                            validBookingIds.add(bookingId);
                            String bookingDate = rs.getString("BOOKING_DATE");
                            int amount = rs.getInt("AMOUNT");
                            String status = rs.getString("STATUS");
                            String paymentMethod = rs.getString("PAYMENT_METHOD");
                            System.out.printf("%-12d %-12s %-8d %-10s %-15s%n",
                                    bookingId, bookingDate, amount, status, paymentMethod);
                        }
                        //step 2: ask user what booking id to select
                        System.out.println("Select BookingID: ");
                        int selection = scanner.nextInt();
                        while(!validBookingIds.contains(selection)){
                            System.out.println("Non valid BookingID selected, Please enter a valid BookingId");
                            selection = scanner.nextInt();
                        }

                        //step 2.5: show the booking again on the console for clarity
                        querySQL = "SELECT * FROM BOOKING WHERE BOOKING_ID="+selection;
                        rs = statement.executeQuery(querySQL);
                        System.out.printf("%-12s %-12s %-8s %-10s %-15s%n",
                                "BookingID", "Date", "Amount", "Status", "Payment");
                        System.out.println("-------------------------------------------------------------");
                        if (rs.next()) {
                            String bookingId = rs.getString("BOOKING_ID");
                            String bookingDate = rs.getString("BOOKING_DATE");
                            int amount = rs.getInt("AMOUNT");
                            String status = rs.getString("STATUS");
                            String paymentMethod = rs.getString("PAYMENT_METHOD");

                            System.out.printf("%-12s %-12s %-8d %-10s %-15s%n",
                                    bookingId, bookingDate, amount, status, paymentMethod);
                        } else {
                            System.out.println("No booking found with that ID.");
                        }

                        //step 3: ask user what status to assign to the booking
                        System.out.println("Select status to assign: \n1)Confirm\n2)Reject\n3)Reschedule\n0)Cancel operation");
                        int secondSelection = scanner.nextInt();
                        while(!validBookingIds.contains(selection)){
                            System.out.println("Non valid status selected, Please enter a valid status");
                            secondSelection = scanner.nextInt();
                        }
                        switch(secondSelection){
                            case 1: //confirm
                                querySQL = "UPDATE BOOKING SET STATUS='CONFIRMED' WHERE BOOKING_ID="+selection;
                                break;
                            case 2: //reject
                                querySQL = "UPDATE BOOKING SET STATUS='REJECTED' WHERE BOOKING_ID="+selection;
                                break;
                            case 3: //reschedule
                                querySQL = "UPDATE BOOKING SET STATUS='RESCHEDULE' WHERE BOOKING_ID="+selection;
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
                        sqlCode = e.getErrorCode(); // Get SQLCODE
                        sqlState = e.getSQLState(); // Get SQLSTATE

                        // Your code to handle errors comes here;
                        // something more meaningful than a print would be good
                        System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                        System.out.println(e);
                    }
                    break;

                case 2:
                    /// Elisha (Option 2)
                    System.out.println("\n--- Creating New Booking ---");
                    System.out.println("1.a) Input service and date wanted:\n");
                    service = scanner.nextLine();
                    System.out.println("Input date wanted (specify format):\n");
                    date = scanner.nextLine();
                    /// query the availabilities
                    System.out.println("Enter preferred slot:\n");
                    int slot = scanner.nextInt();
                    ///  Update DB
                    break;

                case 3:
                    assignEmployeesToSlots(scanner, con);
                    break;

                case 4:
                    System.out.println("\n--- Rescheduling Booking ---");
                    /// query all bookings
                    /// or enter bookings nb? depends how we want to implement it
                    booking_id  = scanner.nextInt();
                    /// query "near" bookings and show potential booking alternatives
                    /// update DB
                    break;

                case 5:
                    System.out.println("\n--- Operational Insights ---");
                    int nestedChoice;
                    System.out.println("\n1) View Bookings By Service");
                    System.out.println("\n2) View Studio Utilization");
                    System.out.println("\n3) View Employee Workload");
                    System.out.println("\n4) View Busiest Working Periods");
                    nestedChoice = scanner.nextInt();
                    switch(nestedChoice){
                        case 1:
                            System.out.println("\n--- Bookings By Service ---");
                            break;
                        case 2:
                            System.out.println("\n--- Studio Utilization ---");
                            break;
                        case 3:
                            System.out.println("\n--- Employee Workload ---");
                            break;
                        case 4:
                            System.out.println("\n--- Busy Working Periods ---");
                            break;
                        default:
                            System.out.println("Invalid Option Selected");
                    }
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        scanner.close();

      // Finally but importantly close the statement and connection
      statement.close ( ) ;
      con.close ( ) ;
    }

    private static void assignEmployeesToSlots(Scanner scanner, Connection con) {        
        System.out.println("\n--- Assigning Staff To a Booking ---");
        
        try {        
            Statement statement = con.createStatement ( ) ;

            //1. Display unassigned EmployeePosition booked slots
            String unassigned_slots = 
                "SELECT * FROM CalendarSlot C " +
                //Service is an EmployeePosition
                //USING tells DB to merge duplicate service_id columns into one
                "JOIN EmployeePosition E USING (service_id) " +
                //Booked 
                "WHERE status = 'Taken' " +
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

            //6. Display updated isAssigned table
            System.out.println("Updated isAssigned table: ");
            String isAssigned = "SELECT * FROM isAssigned";
            printResultSet(statement.executeQuery(isAssigned));

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
