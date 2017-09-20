package io.github.stemlab.dao;

import io.github.stemlab.entity.Column;
import io.github.stemlab.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 9/20/2017.
 */
public interface ConnectionDao {

    void testConnection(String connection, String user, String password) throws SQLException;

    List<String> getSchemas() throws SQLException;

    List<String> getTables(String schema) throws SQLException;

    List<Column> getColumns(String schema, String table) throws SQLException;

    Connection getConnection() throws DatabaseException;

}
