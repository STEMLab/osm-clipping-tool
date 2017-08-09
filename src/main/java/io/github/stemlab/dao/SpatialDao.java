package io.github.stemlab.dao;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.Action;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */

public interface SpatialDao {
    List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope) throws SQLException;

    List<Feature> getKRIntersectsWithTopologyType(Envelope envelope) throws SQLException;

    List<Feature> getUNFeatures() throws SQLException;

    List<Feature> getOSMFeatures() throws SQLException;

    void addToOSM(Feature feature) throws SQLException;

    void replaceObjects(Feature from, Feature to) throws SQLException;

    void deleteObjects(Feature feature) throws SQLException;

    void logAction(String ip, Long osm_id, Action action) throws SQLException;

    void testConnection(String connection, String user, String password) throws SQLException;

    List<String> getSchemas() throws SQLException;

    List<String> getTables(String schema) throws SQLException;

    List<Column> getColumns(String schema, String table) throws SQLException;

    void updateFeature(Feature feature) throws SQLException;
}
