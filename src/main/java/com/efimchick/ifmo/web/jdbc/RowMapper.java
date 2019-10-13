package com.efimchick.ifmo.web.jdbc;

import java.sql.ResultSet;

public interface RowMapper<T> {
    T mapRow(ResultSet resultSet);
}
