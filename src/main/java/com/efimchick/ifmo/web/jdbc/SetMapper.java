package com.efimchick.ifmo.web.jdbc;

import java.sql.ResultSet;

public interface SetMapper<T> {
    T mapSet(ResultSet resultSet);
}
