package io.github.stemlab.service;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.exception.OSMToolException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public interface SpatialService {
    List<Feature> getIntersectionWithTopologyType(Envelope envelope) throws OSMToolException, SQLException;

    List<Feature> getFeatures() throws OSMToolException, SQLException;

    Double getHausdorffDistance(Feature[] features) throws OSMToolException;

    Double getSurfaceDistance(Feature[] features) throws OSMToolException;

    List<Feature> getProcessedFeatures() throws OSMToolException;

    List<Column> getOSMColumnsWithoutMainAttributes() throws SQLException;

    List<Column> getOriginColumnsWithoutMainAttributes() throws SQLException;

    Long generateFeatureId() throws SQLException;

    int getSRID(String schema, String table, String column) throws SQLException;
}
