package io.github.stemlab.service;

import io.github.stemlab.entity.Column;
import io.github.stemlab.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public interface ConnectionService {

    void testConnection(String connection, String user, String password) throws SQLException;

    List<String> getSchemas() throws SQLException;

    List<String> getTables(String schema) throws SQLException;

    List<Column> getColumns(String schema, String table) throws SQLException;

    Connection getConnection() throws DatabaseException;

}
