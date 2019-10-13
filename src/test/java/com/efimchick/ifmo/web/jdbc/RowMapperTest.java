package com.efimchick.ifmo.web.jdbc;

import static com.efimchick.ifmo.web.jdbc.domain.Employee.Parser.parseJson;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

public class RowMapperTest {

    private static ConnectionSource connectionSource;

    @BeforeClass
    public static void initConnectionSource() {
        connectionSource = ConnectionSource.instance();
    }

    @Test
    public void employeeMapRowSingleTest() throws Exception {
        final RowMapper<Employee> employeeRowMapper = new RowMapperFactory().employeeRowMapper();

        try (final Connection conn = connectionSource.createConnection();
             final Statement statement = conn.createStatement();
             final ResultSet rs = statement.executeQuery("select * from EMPLOYEE where id = '7499'")) {

            rs.next();
            final Employee employee = employeeRowMapper.mapRow(rs);

            assertEquals(
                    new Employee(
                            new BigInteger("7499"),
                            new FullName("JOHN", "ALLEN", "MARIA"),
                            Position.SALESMAN,
                            LocalDate.of(1981, 2, 20),
                            new BigDecimal("1600")
                    ),
                    employee
            );
        }
    }

    @Test
    public void employeeMapRowAllTest() throws Exception {
        final RowMapper<Employee> employeeRowMapper = new RowMapperFactory().employeeRowMapper();

        try (final Connection conn = connectionSource.createConnection();
             final Statement statement = conn.createStatement();
             final ResultSet rs = statement.executeQuery("select * from EMPLOYEE")) {

            final Set<Employee> expected = Files.walk(Paths.get("src/test/resources"))
                    .filter(path -> !Files.isDirectory(path))
                    .filter(file -> file.toString().endsWith(".json"))
                    .map(this::employeeFrom)
                    .collect(Collectors.toSet());

            Set<Employee> actual = new HashSet<>();
            while (rs.next()) {
                actual.add(employeeRowMapper.mapRow(rs));
            }

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