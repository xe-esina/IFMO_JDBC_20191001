import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manual transaction management
 */
public class Demo3 {

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

            DriverManager.registerDriver(new org.hsqldb.jdbc.JDBCDriver());

            conn = DriverManager.getConnection(DB_URL);

            conn.createStatement().execute(getSql("init-ddl.sql"));
            conn.createStatement().execute(getSql("init-dml.sql"));

            conn.setAutoCommit(false);

            final ResultSet salRs = conn.createStatement().executeQuery(
                    "SELECT SUM(SALARY) FROM EMPLOYEE " +
                            "where POSITION <> 'PRESIDENT'"
            );
            salRs.next();
            final double totalSalaryExceptPresident = salRs.getDouble(1);
            salRs.close();

            conn.createStatement()
                    .executeUpdate(
                            "UPDATE EMPLOYEE" +
                                    " SET SALARY = SALARY * 0.9" +
                                    " WHERE POSITION != 'PRESIDENT'"
                    );

            String pos = "PRESIDENT";
            final PreparedStatement ps = conn.prepareStatement(
                    "UPDATE EMPLOYEE" +
                            " SET SALARY = SALARY + ? " +
                            " WHERE POSITION = ?"
            );

            ps.setDouble(1, totalSalaryExceptPresident * 0.1);
            ps.setString(2, pos);

            final int presidentRowsUpdatedCount = ps.executeUpdate();
            if(presidentRowsUpdatedCount == 0){
                conn.rollback();
            }
            conn.commit();

            conn.setAutoCommit(true);

            stmt = conn.createStatement();
            String sql;
            sql = "SELECT emp.id, firstname, lastname, salary, d.name as depname " +
                    "FROM EMPLOYEE emp " +
                    "LEFT JOIN DEPARTMENT d on emp.DEPARTMENT = d.ID " +
                    "order by SALARY desc";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String dep = rs.getString("depname");
                double salary = rs.getDouble("salary");

                System.out.print("ID: " + id);
                System.out.print(", First: " + first);
                System.out.print(", Last: " + last);
                System.out.print(", Dep: " + dep);
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
                                Demo3.class.getClassLoader()
                                        .getResourceAsStream(resourceName))))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}




