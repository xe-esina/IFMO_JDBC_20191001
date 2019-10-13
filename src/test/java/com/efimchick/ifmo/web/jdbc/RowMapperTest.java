package com.efimchick.ifmo.web.jdbc;

import static com.efimchick.ifmo.web.jdbc.domain.Employee.Parser.parseJson;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.efimchick.ifmo.web.jdbc.domain.Employee;

public class RowMapperTest {

    private static ConnectionSource connectionSource;

    @BeforeClass
    public static void initConnectionSource() {
        connectionSource = ConnectionSource.instance();
    }

    @Test
    public void employeeMapRowSingleTest() throws Exception {
        testSqlQueryWithRelatedEmployeeSet(
                //language=HSQLDB
                "select * from EMPLOYEE where id = '7499'",
                "src/test/resources/one"
        );
    }

    @Test
    public void employeeMapRowAllTest() throws Exception {
        testSqlQueryWithRelatedEmployeeSet(
                //language=HSQLDB
                "select * from EMPLOYEE order by LASTNAME",
                "src/test/resources/all"
        );
    }

    @Test
    public void employeeMapRowOneDepartmentTest() throws Exception {

        testSqlQueryWithRelatedEmployeeSet(
                //language=HSQLDB
                "select * from EMPLOYEE where DEPARTMENT = 30 order by LASTNAME",
                "src/test/resources/sales"
        );
    }

    private void testSqlQueryWithRelatedEmployeeSet(final String s, final String s2) throws SQLException, IOException {
        final RowMapper<Set<Employee>> employeeRowMapper = new RowMapperFactory().employeesRowMapper();

        try (final Connection conn = connectionSource.createConnection();
             final Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             final ResultSet rs = statement.executeQuery(s)) {

            final Set<Employee> expected = Files.walk(Paths.get(s2))
                    .filter(path -> !Files.isDirectory(path))
                    .filter(file -> file.toString().endsWith(".json"))
                    .map(this::employeeFrom)
                    .collect(Collectors.toSet());

            Set<Employee> actual = employeeRowMapper.mapRow(rs);

            assertEquals(
                    expected,
                    actual
            );
        }
    }

    private Employee employeeFrom(final Path json) {
        try {
            return parseJson(FileUtils.readFileToString(json.toFile(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}