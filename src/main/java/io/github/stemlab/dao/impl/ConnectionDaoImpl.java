package io.github.stemlab.dao.impl;

import io.github.stemlab.dao.ConnectionDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.exception.DatabaseException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Repository
public class ConnectionDaoImpl implements ConnectionDao {

    private static final Logger logger = Logger.getLogger(ConnectionDaoImpl.class);

    @Autowired
    SessionStore sessionStore;
    @Autowired
    SpatialService spatialService;
    @Autowired
    Database database;

    @Override
    public void testConnection(String connection, String user, String password) throws SQLException {
        try {
            Class.forName(database.getDriver());
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found ", e);
            throw new DatabaseException("PostgreSQL driver not found");
        }

        try {
            DriverManager.getConnection(connection, user,
                    password);
        } catch (SQLException e) {
            logger.error("Connection not established ", e);
            throw new DatabaseException("Connection can't be established");
        }
    }

    @Override
    public List<String> getSchemas() throws SQLException {
        List<String> schema = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet schemas = meta.getSchemas();
            while (schemas.next()) {
                String tableSchema = schemas.getString(1); //"TABLE_CATALOG"
                schema.add(tableSchema);
            }
        } catch (SQLException e) {
            logger.error("Exception on getSchemas()", e);
            throw new DatabaseException("Error on getting schemas from DB");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return schema;
    }

    @Override
    public List<String> getTables(String schema) throws SQLException {
        List<String> table = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getConnection();

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, schema, null, new String[]{"TABLE"});
            while (tables.next()) {
                table.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            logger.error("Exception on getTables()", e);
            throw new DatabaseException("Error on getting tables list from DB");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return table;
    }

    @Override
    public List<Column> getColumns(String schema, String table) throws SQLException {
        List<Column> columns = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getColumns(null, schema, table, null);

            ResultSet rs = meta.getPrimaryKeys(null, schema, table);
            String primaryKey = null;
            while (rs.next()) {
                primaryKey = rs.getString("COLUMN_NAME");
            }

            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                String type = resultSet.getString("TYPE_NAME");
                int size = resultSet.getInt("COLUMN_SIZE");
                if (name.equals(primaryKey)) {
                    columns.add(new Column(name, "primary", size));
                } else {
                    columns.add(new Column(name, type, size));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception on getColumns()", e);
            throw new DatabaseException("Error on getting columns of table");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return columns;
    }

    public Connection getConnection() throws DatabaseException {

        Connection dbConnection = null;

        try {
            Class.forName(database.getDriver());
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found", e);
            throw new DatabaseException("PostgreSQL driver not found");
        }

        try {
            dbConnection = DriverManager.getConnection(database.getConnection(), database.getUser(),
                    database.getPassword());
            return dbConnection;
        } catch (SQLException e) {
            logger.error("Fail on connection", e);
            throw new DatabaseException("Connection can't be established");
        }
    }
}
