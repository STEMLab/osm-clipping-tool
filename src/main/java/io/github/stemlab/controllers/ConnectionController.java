package io.github.stemlab.controllers;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.TableWrapper;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 8/1/2017.
 */
@Controller
@RequestMapping(value = "/connect")
public class ConnectionController {

    @Autowired
    SpatialService spatialService;

    @Autowired
    Database database;

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    List<String> testConnection(@RequestParam String name, @RequestParam String port, @RequestParam String password, @RequestParam String host, @RequestParam String user) throws SQLException{

        spatialService.testConnection(name, host, user, port, password);
        database.setDBDefined(true);

        database.setName(name);
        database.setHost(host);
        database.setUser(user);
        database.setPort(port);
        database.setPassword(password);

        return spatialService.getSchemas();
    }

    @RequestMapping(value = "/tables", method = RequestMethod.GET)
    public @ResponseBody
    List<String> getTables(@RequestParam String schema) throws SQLException{
        return spatialService.getTables(schema);
    }

    @RequestMapping(value = "/columns", method = RequestMethod.GET)
    public @ResponseBody
    List<Column> getTables(@RequestParam String schema, @RequestParam String table) throws SQLException{
        return spatialService.getColumns(schema, table);
    }

    @RequestMapping(value = "/schemas", method = RequestMethod.GET)
    public @ResponseBody
    List<String> getSchemas() throws SQLException{
        return spatialService.getSchemas();
    }

    @RequestMapping(value = "/relations", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void setRelations(@RequestBody TableWrapper wrapper) throws SQLException{
        database.setTableWrapper(wrapper);
        database.setRelationDefined(true);
    }
}