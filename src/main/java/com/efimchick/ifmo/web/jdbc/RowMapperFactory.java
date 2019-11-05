package com.efimchick.ifmo.web.jdbc;

import com.efimchick.ifmo.web.jdbc.domain.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class RowMapperFactory {

    public RowMapper<Employee> employeeRowMapper() {
        RowMapper<Employee> rowMapper = new RowMapper<Employee>() {

            @Override
            public Employee mapRow(ResultSet resultSet) {

                try {
                    BigInteger id = new BigInteger(resultSet.getString("ID"));
                    FullName fullName = new FullName(resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"), resultSet.getString("MIDDLENAME"));
                    Position position = Position.valueOf(resultSet.getString("POSITION"));
                    LocalDate hireDate = LocalDate.parse(resultSet.getString("HIREDATE"));
                    BigDecimal salary = resultSet.getBigDecimal("SALARY");

                    return new Employee(id, fullName, position, hireDate, salary);
                }
                catch (SQLException e) {
                    return null;
                }


            }
        };

        return rowMapper;
    }
}
