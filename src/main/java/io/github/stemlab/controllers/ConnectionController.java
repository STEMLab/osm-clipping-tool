package io.github.stemlab.controllers;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.TableWrapper;
import io.github.stemlab.service.ConnectionService;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Bolat Azamat
 * @brief Controller receives all ajax request /connect/*
 * <p>
 * After successful connection to DB, using this API following information can be retrieved:
 * <un>
 * <li>
 * All schemas in connected database
 * </li>
 * <li>
 * Tables in chosen schema
 * </li>
 * <li>
 * Columns in chosen table
 * </li>
 * <li>
 * Relations of two tables
 * @see TableWrapper
 * </li>
 * </un>
 */
@Controller
@RequestMapping(value = "/connect")
public class ConnectionController {

    @Autowired
    SpatialService spatialService;

    @Autowired
    Database database;

    @Autowired
    ConnectionService connectionService;

    /**
     * Test connection, if all params right DB set defined, and schemas returned, otherwise @throws {@link SQLException}
     *
     * @param name     db name
     * @param user     user name
     * @param password user password
     * @param host     host on which db is running
     * @param port     db port
     * @return existed schemas in connected db
     * @throws SQLException
     */
    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<String> testConnection(@RequestParam String name, @RequestParam String port, @RequestParam String password, @RequestParam String host, @RequestParam String user) throws SQLException {

        connectionService.testConnection(host, port, name, user, password);
        database.setDBDefined(true);

        database.setName(name);
        database.setHost(host);
        database.setUser(user);
        database.setPort(port);
        database.setPassword(password);

        return connectionService.getSchemas();
    }

    /**
     * Get tables in schema
     *
     * @param schema
     * @return list of table names in schema
     * @throws SQLException
     */
    @RequestMapping(value = "/tables", method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> getTables(@RequestParam String schema) throws SQLException {
        return connectionService.getTables(schema);
    }

    /**
     * Get columns in table
     *
     * @param schema
     * @param table
     * @return list of columns in tables @see {@link Column}
     * @throws SQLException
     */
    @RequestMapping(value = "/columns", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Column> getColumns(@RequestParam String schema, @RequestParam String table) throws SQLException {
        return connectionService.getColumns(schema, table);
    }

    /**
     * Get schemas in db
     *
     * @return list of schemas in database
     * @throws SQLException
     */
    @RequestMapping(value = "/schemas", method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> getSchemas() throws SQLException {
        return connectionService.getSchemas();
    }

    /**
     * Define relation of two tables
     *
     * @param wrapper
     * @throws SQLException
     * @see TableWrapper
     */
    @RequestMapping(value = "/relations", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void setRelations(@RequestBody TableWrapper wrapper) throws SQLException {
        database.setTableWrapper(wrapper);
        database.setRelationDefined(true);
    }
}
