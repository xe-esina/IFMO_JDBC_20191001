package com.efimchick.ifmo.web.jdbc.service;

import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.Department;
import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class ServiceFactory {
    public EmployeeService employeeService() {
        return new EmployeeService() {

            private List<Employee> getPage (List<Employee> list, Paging paging) {
                // Так епта, что это за пейджинги такие?
                // Просто 2 числа — itemsPerPage (кол-во строк на странице) и page (номер страницы)
                // Это чтобы из результата выбрать конкретный блок данных (страницу) и мы помучались
                // Так что просто грустнеем на одну функцию

                return list.subList(paging.itemPerPage * (paging.page - 1),
                                    min (list.size(), paging.itemPerPage * paging.page));
            }

            private List<Employee> getResultList (String request) {
                try {
                    ResultSet resultSet = ConnectionSource.instance().createConnection().createStatement().executeQuery(request);
                    List<Employee> result = new ArrayList<>();

                    while (resultSet.next()) {
                        result.add(employeeMapRow(resultSet, false, false));
                    }

                    return result;
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

            private Employee employeeMapRow(ResultSet resultSet, boolean isManager, boolean isFullChain) {
                // Вот тут сложно — надо еще засовывать объекты менеджеров и департамента
                try {
                    BigInteger id = new BigInteger(resultSet.getString("ID"));
                    FullName fullName = new FullName(resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"), resultSet.getString("MIDDLENAME"));
                    Position position = Position.valueOf(resultSet.getString("POSITION"));
                    LocalDate hireDate = LocalDate.parse(resultSet.getString("HIREDATE"));
                    BigDecimal salary = resultSet.getBigDecimal("SALARY");

                    // Дальше запутанно
                    // isManager — менеджер ли текущий чел. Если да — обрываем цепочку
                    // Когда isFullChain, хитрим и всех считаем не менеджерами, и получаем полную цепочку

                    Employee manager = null;
                    if (!isManager) {
                        int managerId = resultSet.getInt("MANAGER");

                        ResultSet managerResultSet = ConnectionSource.instance().createConnection().createStatement()
                                .executeQuery("SELECT * FROM employee WHERE id = " + managerId);

                        // Постарайтесь сами понять, почему тут isManager = !isFullChain
                        if (managerResultSet.next())
                            manager = employeeMapRow(managerResultSet, !isFullChain, isFullChain);
                    }

                    // Тут попроще, просто мапим департамент по айди
                    Department department = null;

                    int departmentId = resultSet.getInt("DEPARTMENT");

                    ResultSet departmentResultSet = ConnectionSource.instance().createConnection().createStatement()
                            .executeQuery("SELECT * FROM department WHERE id = " + departmentId);

                    if (departmentResultSet.next())
                        department = departmentMapRow(departmentResultSet);

                    return new Employee(id, fullName, position, hireDate, salary, manager, department);
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public List<Employee> getAllSortByHireDate(Paging paging) {
                return getPage(getResultList("SELECT * FROM employee ORDER BY hiredate"), paging);
            }

            @Override
            public List<Employee> getAllSortByLastname(Paging paging) {
                return getPage(getResultList("SELECT * FROM employee ORDER BY lastname"), paging);
            }

            @Override
            public List<Employee> getAllSortBySalary(Paging paging) {
                return getPage(getResultList("SELECT * FROM employee ORDER BY salary"), paging);
            }

            @Override
            public List<Employee> getAllSortByDepartmentNameAndLastname(Paging paging) {
                return getPage(getResultList("SELECT * FROM employee ORDER BY department, lastname"), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByHireDate(Department department, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY hiredate"), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortBySalary(Department department, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY salary"), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByLastname(Department department, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY lastname"), paging);
            }

            @Override
            public List<Employee> getByManagerSortByLastname(Employee manager, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY lastname"), paging);
            }

            @Override
            public List<Employee> getByManagerSortByHireDate(Employee manager, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY hiredate"), paging);
            }

            @Override
            public List<Employee> getByManagerSortBySalary(Employee manager, Paging paging) {
                return getPage(getResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY salary"), paging);
            }

            @Override
            public Employee getWithDepartmentAndFullManagerChain(Employee employee) {
                // Дублируем getResultList, только нам нужен один объект, и isFull теперь true
                try {
                    ResultSet resultSet = ConnectionSource.instance().createConnection().createStatement()
                            .executeQuery("SELECT * FROM employee WHERE id = " + employee.getId());

                    if (resultSet.next()) {
                        return employeeMapRow(resultSet, false, true);
                    }
                    else
                        return null;
                }
                catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public Employee getTopNthBySalaryByDepartment(int salaryRank, Department department) {
                return getResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY salary DESC")
                        .get(salaryRank - 1);
            }
        };
    }
}
