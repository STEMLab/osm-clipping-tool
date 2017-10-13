package io.github.stemlab.dao;

import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.sql.SQLException;
import java.util.List;

/**
 * @brief Interface, for access to database, to get spatial information
 *
 * @see io.github.stemlab.dao.impl.SpatialDaoImpl
 * @see io.github.stemlab.service.SpatialService
 * @author Bolat Azamat
 */
public interface SpatialDao {

    /**
     * Build and execute query to get intersected target features with give envelope
     * @param envelope
     * @return list of features
     * @throws SQLException
     */
    List<Feature> getTargetIntersectionWithTopologyType(Envelope envelope) throws SQLException;

    /**
     * Build and execute query to get intersected source features with given envelope
     * @param envelope
     * @return
     * @throws SQLException
     */
    List<Feature> getSourceIntersectionWithTopologyType(Envelope envelope) throws SQLException;

    /**
     * Build and execute query to get all source features
     * @return
     * @throws SQLException
     */
    List<Feature> getSourceFeatures() throws SQLException;

    /**
     * Build and execute query to get all target features
     * @return
     * @throws SQLException
     */
    List<Feature> getTargetFeatures() throws SQLException;

    /**
     * Build and
     * @return
     * @throws SQLException
     */
    Long generateFeatureId() throws SQLException;

    int getSRID(String schema, String table, String column) throws SQLException;
}
