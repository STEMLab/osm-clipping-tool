package io.github.stemlab.dao;

import io.github.stemlab.entity.Feature;

import java.sql.SQLException;


public interface TargetTableDao {
    void add(Feature feature) throws SQLException;

    void replace(Feature source, Feature target) throws SQLException;

    void delete(Feature feature) throws SQLException;

    void update(Feature feature) throws SQLException;
}
