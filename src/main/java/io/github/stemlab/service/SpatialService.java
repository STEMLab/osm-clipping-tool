package io.github.stemlab.service;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.exception.OSMToolException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public interface SpatialService {
    List<Feature> getIntersectsWithTopology(Envelope envelope) throws OSMToolException, SQLException;

    List<Feature> getFeatures() throws OSMToolException, SQLException;

    void addToOsmDataSet(Feature[] features) throws OSMToolException, SQLException;

    void replaceObjects(Feature[] features) throws OSMToolException, SQLException;

    void updateFeature(Feature feature) throws OSMToolException, SQLException;

    void deleteObjects(Feature[] features) throws OSMToolException, SQLException;

    Double getHausdorffDistance(Feature[] features) throws OSMToolException;

    Double getSurfaceDistance(Feature[] features) throws OSMToolException;

    List<Feature> getProcessedFeatures() throws OSMToolException;

    void logAction(String ip, Long osm_id, Action action);

    void testConnection(String name, String host, String user, String port, String password) throws SQLException;

    List<String> getSchemas() throws SQLException;

    List<String> getTables(String schema) throws SQLException;

    List<Column> getColumns(String schema, String table) throws SQLException;

    List<Column> getOSMColumnsWithoutMainAttributes() throws SQLException;

    List<Column> getOriginColumnsWithoutMainAttributes() throws SQLException;
}
