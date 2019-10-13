package com.efimchick.ifmo.web.jdbc.dao;

import java.math.BigInteger;
import java.util.List;

import com.efimchick.ifmo.web.jdbc.domain.Department;
import com.efimchick.ifmo.web.jdbc.domain.Employee;

public interface EmployeeDao extends Dao<Employee, BigInteger> {
    List<Employee> getByDepartment(Department department);
    List<Employee> getByManager(Employee employee);

}

