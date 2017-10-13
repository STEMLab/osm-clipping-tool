package io.github.stemlab.dao;

import io.github.stemlab.entity.Column;
import io.github.stemlab.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @brief Interface, for access to database, to get connection details
 *
 * @see io.github.stemlab.dao.impl.ConnectionDaoImpl
 * @see io.github.stemlab.service.ConnectionService
 * @author Bolat Azamat
 */
public interface ConnectionDao {

    /**
     *  Query to database using given params
     * @param connection concatenated string by service for driver, url and port
     * @param user user login
     * @param password user password
     * @throws SQLException
     */
    void testConnection(String connection, String user, String password) throws SQLException;

    /**
     *  Query to database to get available schemas
     * @return
     * @throws SQLException
     */
    List<String> getSchemas() throws SQLException;

    /**
     *  Query to database to get tables in given schema
     * @param schema
     * @return
     * @throws SQLException
     */
    List<String> getTables(String schema) throws SQLException;

    /**
     *  Query ti database to get columns in give table
     * @param schema
     * @param table
     * @return
     * @throws SQLException
     */
    List<Column> getColumns(String schema, String table) throws SQLException;

    /**
     * Get connection to database
     * @return
     * @throws DatabaseException
     */
    Connection getConnection() throws DatabaseException;

}
