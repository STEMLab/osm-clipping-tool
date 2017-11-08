package io.github.stemlab.service;

import io.github.stemlab.entity.Feature;
import io.github.stemlab.exception.OSMToolException;

import java.sql.SQLException;

/**
 * Created by Azamat on 9/21/2017.
 */
public interface TargetTableService {
    void add(Feature[] features) throws SQLException, OSMToolException;

    void replace(Feature[] features) throws SQLException, OSMToolException;

    void delete(Feature[] features) throws SQLException, OSMToolException;

    void update(Feature feature) throws SQLException;
}

