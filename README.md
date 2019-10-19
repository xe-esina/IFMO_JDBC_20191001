# IFMO_JDBC_20191001
[![from_flaxo with_♥](https://img.shields.io/badge/from_flaxo-with_♥-blue.svg)](https://github.com/tcibinan/flaxo)

## task-5 Service (Repository)
Implement `com.efimchick.ifmo.web.jdbc.service.ServiceFactory` method.

It should return an Employee service instance.

Services often performs as a layer between DAO and controllers as it is suggested by classical 3-layer architecture design.
Although main function of a Service is considered to be a provision of business logic so mature architecture approaches like Clean Architecture, DDD specifies that a Service should not depend on persistence layer.. 
So one may consider a Employee Service you need to implement to be really a Repository approach. 

Anyway, you should implement EmployeeService  interface.

Important: Usually you have to provide an employee as an object of Employee class, having injected Department object and injected Employee object referencing to his manager.
Object of a manager should be having injected Department as well, but his own manager should not be injected.
Trying to show it in graphical way:
```
employee
    |-department
    |-manager
        |-department
```
Though, implementation of `getWithDepartmentAndFullManagerChain` method requires extracting of an employee with full management chain 
meaning that it should have a manager who should have a manager who should have a manager and so on until top manager is reached.
So, it will look like:
```
employee
    |-department
    |-manager
        |-department
        |-manager
            |-department
            |-manager
                |-department
                |-manager
                    |-department
                    |-manager
                        ...
```
 


P.S. You may not alter domain classes or anything from test, just a reminder.  
 