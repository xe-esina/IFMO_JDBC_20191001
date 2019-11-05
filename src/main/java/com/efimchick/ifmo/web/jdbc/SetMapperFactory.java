package com.efimchick.ifmo.web.jdbc;

import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Set;
import java.util.HashSet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

public class SetMapperFactory {

    public SetMapper<Set<Employee>> employeesSetMapper() {

        SetMapper<Set<Employee>> resultMap = new SetMapper<Set<Employee>>() {

            @Override
            public Set<Employee> mapSet(ResultSet resultSet) {

                Set<Employee> employeeSet = new HashSet<>();
                try {
                    while (resultSet.next())
                        employeeSet.add(mapRow(resultSet));

                    return employeeSet;
                }
                catch (SQLException e) {
                    return null;
                }
            }
        };

        return resultMap;
    }

    public Employee mapRow(ResultSet resultSet) {

        try {
            // Мэппинг простых вещей из прошлого таска
            BigInteger id = new BigInteger(resultSet.getString("ID"));
            FullName fullName = new FullName(resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"), resultSet.getString("MIDDLENAME"));
            Position position = Position.valueOf(resultSet.getString("POSITION"));
            LocalDate hireDate = LocalDate.parse(resultSet.getString("HIREDATE"));
            BigDecimal salary = resultSet.getBigDecimal("SALARY");

            // Собираем менеджеров всех менеджеров
            Employee manager = null;

            if (resultSet.getString("MANAGER") != null) {
                // Ищем непосредственного менеджера нашего чувачка в таблице
                int managerId = resultSet.getInt("MANAGER");

                // Запоминаем, где были, чтобы потом вернуться
                int curRow = resultSet.getRow();
                // Встаем на начало таблицы
                resultSet.absolute(0);

                while (resultSet.next()) {
                    if (managerId == resultSet.getInt("ID"))
                        break;
                }

                // Собираем менеджера таким же образом
                manager = mapRow(resultSet);

                // Возвращаемся на место!
                resultSet.absolute(curRow);
            }

            // Собираем чувачка
            return new Employee(id, fullName, position, hireDate, salary, manager);
        } catch (SQLException e) {
            return null;
        }
    }
}
