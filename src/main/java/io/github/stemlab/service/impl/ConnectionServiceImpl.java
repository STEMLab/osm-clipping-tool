package io.github.stemlab.service.impl;

import io.github.stemlab.dao.ConnectionDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.exception.DatabaseException;
import io.github.stemlab.service.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 9/20/2017.
 */
@Service
public class ConnectionServiceImpl implements ConnectionService {

    @Autowired
    ConnectionDao connectionDao;

    @Override
    public void testConnection(String host, String port, String name, String user, String password) throws SQLException {
        connectionDao.testConnection("jdbc:postgresql://" + host + ":" + port + "/" + name, user, password);
    }

    @Override
    public List<String> getSchemas() throws SQLException {
        return connectionDao.getSchemas();
    }

    @Override
    public List<String> getTables(String schema) throws SQLException {
        return connectionDao.getTables(schema);
    }

    @Override
    public List<Column> getColumns(String schema, String table) throws SQLException {
        return connectionDao.getColumns(schema, table);
    }

    @Override
    public Connection getConnection() throws DatabaseException {
        return connectionDao.getConnection();
    }
}
