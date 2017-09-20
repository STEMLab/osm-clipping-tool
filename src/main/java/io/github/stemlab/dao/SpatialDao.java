package io.github.stemlab.dao;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */

public interface SpatialDao {
    List<Feature> getTargetIntersectionWithTopologyType(Envelope envelope) throws SQLException;

    List<Feature> getSourceIntersectionWithTopologyType(Envelope envelope) throws SQLException;

    List<Feature> getSourceFeatures() throws SQLException;

    List<Feature> getTargetFeatures() throws SQLException;

    Long generateFeatureId() throws SQLException;

    int getSRID(String schema, String table, String column) throws SQLException;
}
