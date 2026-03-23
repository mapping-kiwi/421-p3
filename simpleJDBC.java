import java.sql.* ;
import java.util.Scanner;

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

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2026-comp421.cs.mcgill.ca:50000/comp421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = System.getenv("DB421_USER");
        String your_password = System.getenv("DB421_PASSWORD");
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd 
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

        // Creating a table
//        try
//        {
//          String createSQL = "CREATE TABLE " + tableName + " (id INTEGER, name VARCHAR (25)) ";
//          System.out.println (createSQL ) ;
//          statement.executeUpdate (createSQL ) ;
//          System.out.println ("DONE");
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
//         }

        // Inserting Data into the table
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
//          // Your code to handle errors comes here;
//          // something more meaningful than a print would be good
//          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
//          System.out.println(e);
//        }

        // Querying a table
        try
        {
          String querySQL = "SELECT BOOKING_ID, AMOUNT FROM BOOKING";
          System.out.println (querySQL) ;
          java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

          while ( rs.next ( ) )
          {
            int id = rs.getInt ( 1 ) ;
            String amt = rs.getString (2);
            System.out.println ("id:  " + id);
            System.out.println ("amount:  " + amt);
          }
         System.out.println ("DONE");
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

      //Updating a table
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
            System.out.println("0) Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            //switch vars
            String date;
            String service;
            int booking_id;

            switch (choice) {

                case 1:
                    /// i lowkey dont remember who this is
                    System.out.println("\n--- View and/or Process Booking Requests ---");
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
                    System.out.println("\n--- Assigning Staff To a Booking ---");
                    /// query all bookings
                    /// or enter bookings nb? depends how we want to implement it
                    booking_id  = scanner.nextInt();
                    ///  query employees available at a specific session
                    /// Update DB
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
}
