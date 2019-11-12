package com.efimchick.ifmo.web.jdbc.dao;

import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.Department;
import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoFactory {
    // MapRow-ы, чтобы из запроса собрать объект и его пихать в список
    private Employee employeeMapRow(ResultSet resultSet) {
        try {
            BigInteger id = new BigInteger(resultSet.getString("ID"));
            FullName fullName = new FullName(resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"), resultSet.getString("MIDDLENAME"));
            Position position = Position.valueOf(resultSet.getString("POSITION"));
            LocalDate hireDate = LocalDate.parse(resultSet.getString("HIREDATE"));
            BigDecimal salary = resultSet.getBigDecimal("SALARY");
            BigInteger managerId = BigInteger.valueOf(resultSet.getInt("MANAGER"));
            BigInteger departmentId = BigInteger.valueOf(resultSet.getInt("DEPARTMENT"));

            return new Employee(id, fullName, position, hireDate, salary, managerId, departmentId);
        }
        catch (SQLException e) {
            return null;
        }
    }

    private Department departmentMapRow(ResultSet resultSet) {
        try {
            BigInteger id = new BigInteger(resultSet.getString("ID"));
            String name = resultSet.getString("NAME");
            String location = resultSet.getString("LOCATION");

            return new Department(id, name, location);
        }
        catch (SQLException e) {
            return null;
        }
    }

    // Обрабатываем и забираем resultSet
    private ResultSet executeSQL(String request) {
        try {
            return ConnectionSource.instance().createConnection().createStatement().executeQuery(request);
        }
        catch (SQLException e) {
            return null;
        }
    }

    public EmployeeDao employeeDAO() {
        return new EmployeeDao() {
            @Override
            public List<Employee> getByDepartment(Department department) {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM employee WHERE department = " + department.getId());

                    List<Employee> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(employeeMapRow(resultSet));
                    }

                    return result;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public List<Employee> getByManager(Employee employee) {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM employee WHERE manager = " + employee.getId());

                    List<Employee> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(employeeMapRow(resultSet));
                    }

                    return result;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public Optional<Employee> getById(BigInteger Id) {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM employee WHERE id = " + Id.toString());

                    if (resultSet.next())
                        return Optional.of(employeeMapRow(resultSet));
                    else
                        return Optional.empty();
                }
                catch (SQLException e) {
                    return Optional.empty();
                }
            }

            @Override
            public List<Employee> getAll() {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM employee");

                    List<Employee> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(employeeMapRow(resultSet));
                    }

                    return result;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public Employee save(Employee employee) {
                try {
                    PreparedStatement prepStat = ConnectionSource.instance().createConnection().prepareStatement("INSERT INTO employee VALUES (?,?,?,?,?,?,?,?,?)");

                    prepStat.setInt(1, employee.getId().intValue());
                    prepStat.setString(2, employee.getFullName().getFirstName());
                    prepStat.setString(3, employee.getFullName().getLastName());
                    prepStat.setString(4, employee.getFullName().getMiddleName());
                    prepStat.setString(5, employee.getPosition().toString());
                    prepStat.setInt(6, employee.getManagerId().intValue());
                    prepStat.setDate(7, Date.valueOf(employee.getHired()));
                    prepStat.setDouble(8,

                            employee.getSalary().doubleValue());
                    prepStat.setInt(9, employee.getDepartmentId().intValue());

                    prepStat.executeUpdate();
                    return employee;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public void delete(Employee employee) {
                try {
                    ConnectionSource.instance().createConnection().createStatement().execute("DELETE FROM employee WHERE ID = " + employee.getId().toString());
                }
                catch (SQLException e) {
                    System.out.println("delete error");
                }
            }
        };
    }

    public DepartmentDao departmentDAO() {
        return new DepartmentDao() {
            @Override
            public Optional<Department> getById(BigInteger Id) {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM department WHERE id = " + Id.toString());

                    if (resultSet.next())
                        return Optional.of(departmentMapRow(resultSet));
                    else
                        return Optional.empty();
                }
                catch (SQLException e) {
                    return Optional.empty();
                }

            }

            @Override
            public List<Department> getAll() {
                try {
                    ResultSet resultSet = executeSQL("SELECT * FROM department");

                    List<Department> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(departmentMapRow(resultSet));
                    }

                    return result;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public Department save(Department department) {
                try {
                    PreparedStatement prepStat;

                    if (getById(department.getId()).equals(Optional.empty())) {
                        prepStat = ConnectionSource.instance().createConnection().prepareStatement("INSERT INTO department VALUES (?,?,?)");

                        prepStat.setInt(1, department.getId().intValue());
                        prepStat.setString(2, department.getName());
                        prepStat.setString(3, department.getLocation());
                    }
                    else {
                        prepStat = ConnectionSource.instance().createConnection().prepareStatement("UPDATE department SET NAME = ?, LOCATION = ? WHERE ID = ?");

                        prepStat.setString(1, department.getName());
                        prepStat.setString(2, department.getLocation());
                        prepStat.setInt(3, department.getId().intValue());
                    }

                    prepStat.executeUpdate();
                    return department;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public void delete(Department department) {
                try {
                    ConnectionSource.instance().createConnection().createStatement().execute("DELETE FROM department WHERE ID = " + department.getId().toString());
                }
                catch (SQLException e) {}
            }
        };
    }
}