import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

public class Demo2 {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    private static final String DB_URL = "jdbc:hsqldb:mem:myDb";

    //  Database credentials
    private static final String USER = "sa";
    private static final String PASS = "sa";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
//            Class.forName(JDBC_DRIVER);
            DriverManager.registerDriver(new org.hsqldb.jdbc.JDBCDriver());

            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            conn.createStatement().execute(getSql("init-ddl.sql"));
            conn.createStatement().execute(getSql("init-dml.sql"));


            conn.createStatement().execute("UPDATE EMPLOYEE SET SALARY=SALARY+500 WHERE SALARY < 2000");


            stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, firstname, lastname, salary FROM Employee";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                int salary = rs.getInt("salary");

                System.out.print("ID: " + id);
                System.out.print(", First: " + first);
                System.out.print(", Last: " + last);
                System.out.println(", Salary: " + salary);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    private static String getSql(final String resourceName) {
        return new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                Demo2.class.getClassLoader().getResourceAsStream(resourceName))))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}




